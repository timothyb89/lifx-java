package org.timothyb89.lifx.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.PacketFactory;
import org.timothyb89.lifx.net.packet.handler.PacketHandler;

/**
 * Defines a basic gateway. This class maintains a TCP connection to a gateway
 * bulb which then dispatches commands to its connected bulbs (or potentially
 * itself).
 * @author tim
 */
@Slf4j
@ToString
public class Gateway implements EventBusProvider {
	
	@Getter private final InetSocketAddress ipAddress;
	@Getter private final int port;
	@Getter private final MACAddress macAddress;

	private final Deque<PacketResponseFuture> responses;
	
	private final EventBus bus;
	
	private SocketChannel channel;
	private Thread listenerThread;

	public Gateway(InetSocketAddress ipAddress, int port, MACAddress macAddress) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.macAddress = macAddress;
		
		responses = new ConcurrentLinkedDeque<>();
		
		bus = new EventBus() {{
			add(GatewayPacketSentEvent.class);
			add(GatewayPacketReceivedEvent.class);
		}};
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
	
	public Future<PacketResponse> send(Packet packet) throws IOException {
		PacketResponseFuture f = new PacketResponseFuture(packet);
		responses.offer(f);
		
		channel.write(packet.bytes());
		
		bus.push(new GatewayPacketSentEvent(packet, f));
		
		return f;
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
					
					log.info("reading {} additional bytes ...", size);
					
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
					
					log.info("Packet type {} received", String.format("0x%02X", type));
					
					// attempt to handle the packet
					PacketHandler handler = PacketFactory.createHandler(type);
					if (handler == null) {
						log.warn("Unknown packet type: {}",
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
					
					// is this a response?
					PacketResponseFuture recipient = null;
					for (PacketResponseFuture f : responses) {
						if (f.expectsResponse(type)) {
							log.info("Response expected packet type {}",
									String.format("0x%02X", type));
							recipient = f;
							break;
						}
					}
					
					// notify the recipient, if any
					if (recipient != null) {
						recipient.putResponse(packet);

						if (recipient.isFulfilled()) {
							log.info("Fulfilled response");
							responses.remove(recipient);
							bus.push(new GatewayResponseFulfilledEvent(recipient));
						}
					}
					
				} catch (IOException ex) {
					log.error("Error reading packet", ex);
				}
			}
		}
		
	};
	
}
