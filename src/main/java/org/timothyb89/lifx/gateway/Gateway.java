package org.timothyb89.lifx.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.eventbus.EventHandler;
import org.timothyb89.lifx.bulb.Bulb;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.PacketFactory;
import org.timothyb89.lifx.net.packet.handler.PacketHandler;
import org.timothyb89.lifx.net.packet.request.LightStatusRequest;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;

/**
 * Defines a basic gateway. This class maintains a TCP connection to a gateway
 * bulb which then dispatches commands to its connected bulbs (or potentially
 * itself).
 * @author tim
 */
@Slf4j
@ToString(of = { "ipAddress", "port", "macAddress", "bulbs" })
public class Gateway implements EventBusProvider {
	
	@Getter private final InetSocketAddress ipAddress;
	@Getter private final int port;
	@Getter private final MACAddress macAddress;

	private final Deque<PacketResponseFuture> responses;
	
	private final EventBus bus;
	
	private SocketChannel channel;
	private Thread listenerThread;
	
	private List<Bulb> bulbs;

	public Gateway(InetSocketAddress ipAddress, int port, MACAddress macAddress) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.macAddress = macAddress;
		
		responses = new ConcurrentLinkedDeque<>();
		
		bulbs = Collections.synchronizedList(new LinkedList<Bulb>());
		
		bus = new EventBus() {{
			add(GatewayConnectedEvent.class);
			add(GatewayDisconnectedEvent.class);
			add(GatewayPacketSentEvent.class);
			add(GatewayPacketReceivedEvent.class);
			add(GatewayBulbDiscoveredEvent.class);
		}};
		
		bus.register(this);
	}

	@Override
	public EventBusClient bus() {
		return bus.getClient();
	}
	
	public void connect() throws IOException {
		channel = SocketChannel.open();
		channel.connect(new InetSocketAddress(ipAddress.getAddress(), port));
		
		listenerThread = new Thread(listener, "lifx-tcp-listen");
		listenerThread.start();
		
		bus.push(new GatewayConnectedEvent(this));
		
		// try to find available bulbs
		send(new LightStatusRequest());
	}
	
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}
	
	public void disconnect() {
		try {
			channel.close();
		} catch (IOException ex) {
			log.error("Error closing socket", ex);
		}
	}
	
	/**
	 * Sends the given raw packet. Most commands require a set {@code site}
	 * field; {@link #send(Packet)} will automatically set this to the address
	 * of this gateway. This method leaves the {@code site} field of the packet
	 * unmodified and send the packet with whatever site field it has currently
	 * set.
	 * <p>If applicable, a Future containing a {@link PacketResponse} object is
	 * returned with any response packets. Expected packet types must first be
	 * configured in {@link Packet#expectedResponses()}. Note that most commands
	 * that trigger responses cannot be used with this mechanism as responses
	 * are not guaranteed.</p>
	 * @param packet the packet to send
	 * @return a Future containing any packet responses
	 * @throws IOException 
	 */
	public PacketResponseFuture sendRaw(Packet packet) throws IOException {
		PacketResponseFuture f = new PacketResponseFuture(packet);
		responses.offer(f);
		
		channel.write(packet.bytes());
		
		bus.push(new GatewayPacketSentEvent(packet, f));
		
		return f;
	}
	
	/**
	 * Sends the given packet to the gateway. The {@code site} field of the
	 * packet will be set to the address of this gateway; if this is undesired,
	 * use {@link #sendRaw(Packet)}. Generally, this will result in the packet
	 * being distributed to all bulbs connected to the gateway, and can result
	 * in one (or many) responses.
	 * <p>Note that you <b>cannot</b> wait on a future from an event
	 * notification: events are dispatched on the input thread, so a blocking
	 * wait <i>will</i> cause a deadlock. As a general rule blocking on the
	 * event thread is discouraged; a different (or new) thread should be used,
	 * and is a requirement in most contexts here.</p>
	 * @see #sendRaw(Packet)
	 * @param packet the packet to send
	 * @return a Future containing any packet responses
	 * @throws IOException 
	 */
	public Future<PacketResponse> send(Packet packet) throws IOException {
		packet.setSite(macAddress);
		return sendRaw(packet);
	}
	
	public List<Bulb> getBulbs() {
		return Collections.unmodifiableList(bulbs);
	}
	
	public Bulb getBulb(MACAddress address) {
		for (Bulb b : bulbs) {
			if (b.getAddress().getHex().equalsIgnoreCase(address.getHex())) {
				return b;
			}
		}
		
		return null;
	}
	
	@EventHandler
	public void bulbDiscoveryListener(GatewayPacketReceivedEvent event) {
		Packet p = event.getPacket();
		
		if (p instanceof LightStatusResponse) {
			LightStatusResponse resp = (LightStatusResponse) p;
			
			Bulb bulb = getBulb(resp.getBulbAddress());
			if (bulb == null) {
				bulb = new Bulb(this, resp.getBulbAddress());
				bulb.valuesFromPacket(resp);
				bulbs.add(bulb);
				
				log.debug("Bulb discovered: {}", bulb);
				
				bus.push(new GatewayBulbDiscoveredEvent(this, bulb));
			}
		}
	}
	
	private Runnable listener = new Runnable() {

		@Override
		public void run() {
			while (true) {
				try {
					ByteBuffer sizeBuffer = ByteBuffer.allocate(2);
					int read = channel.read(sizeBuffer);
					if (read < 2) {
						log.error("TCP listener reached EOF, terminating...");
						break;
					}
					
					sizeBuffer.rewind();
					
					int size = Packet.FIELD_SIZE.value(sizeBuffer);
					
					sizeBuffer.rewind();
					
					log.debug("Reading {} additional bytes ...", size);
					
					// append the size back into the final buffer, so we get
					// a full packet (for the parser)
					ByteBuffer buf = ByteBuffer.allocate(size);
					buf.put(sizeBuffer);
	
					channel.read(buf);
					buf.rewind();
					
					// extract the packet type (as in BroadcastListener)
					ByteBuffer packetType = buf.slice();
					packetType.position(32);
					packetType.limit(34);
					int type = Packet.FIELD_PACKET_TYPE.value(packetType);
					
					log.debug("Packet type {} received", String.format("0x%02X", type));
					
					// attempt to handle the packet
					PacketHandler handler = PacketFactory.createHandler(type);
					if (handler == null) {
						log.debug("Unknown packet type: {}",
								String.format("0x%02X", type));
						continue;
					}
					
					Packet packet = handler.handle(buf);
					if (packet == null) {
						log.warn("Handler {} was unable to handle packet",
								handler.getClass().getName());
						continue;
					}
					
					bus.push(new GatewayPacketReceivedEvent(packet));
					
					// clean up fulfilled (empty) response futures
					List<PacketResponseFuture> toRemove = new LinkedList<>();
					
					// is this a response?
					PacketResponseFuture recipient = null;
					for (PacketResponseFuture f : responses) {
						if (f.isFulfilled()) {
							toRemove.add(f);
							continue;
						}
						
						if (recipient == null&& f.expectsResponse(
								type, packet.getBulbAddress())) {
							
							log.debug("Response expected packet type {}",
									String.format("0x%02X", type));
							
							recipient = f;
						}
					}
					
					responses.removeAll(toRemove);
					
					// notify the recipient, if any
					if (recipient != null) {
						recipient.putResponse(packet);

						if (recipient.isFulfilled()) {
							log.debug("Fulfilled response");
							responses.remove(recipient);
							bus.push(new GatewayResponseFulfilledEvent(recipient));
						}
					}
					
				} catch (IOException ex) {
					log.error("Error reading packet", ex);
				}
			}
			
			bus.push(new GatewayDisconnectedEvent(Gateway.this));
		}
		
	};
	
}
