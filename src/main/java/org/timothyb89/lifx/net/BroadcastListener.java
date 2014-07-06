package org.timothyb89.lifx.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.GatewayManager;
import org.timothyb89.lifx.net.android.WifiManagerProxy;
import org.timothyb89.lifx.net.android.WifiManagerProxy.MulticastLockProxy;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.PacketFactory;
import org.timothyb89.lifx.net.packet.handler.PacketHandler;
import org.timothyb89.lifx.net.packet.request.PANGatewayRequest;
import org.timothyb89.lifx.net.packet.response.PANGatewayResponse;

/**
 * Listens for UDP broadcasts from gateway bulbs. Listening and broadcasting
 * can be started with {@link #startListen()}, and then events of type
 * {@link GatewayDiscoveredEvent} will be emitted as gateways are discovered.
 * <p>Currently only gateway discovery is performed over UDP; however, a number
 * of events are sent over UDP in addition to TCP. In the future these may be
 * handled to remove some of the need for maintaining a TCP connection.</p>
 * @author tim
 */
@Slf4j
public class BroadcastListener implements EventBusProvider {
	
	public static final int BROADCAST_PORT = 56700;
	public static final int BROADCAST_DELAY = 1000;
	
	/**
	 * The address used for broadcast packets, in this case the entire /0 subnet
	 */
	public static final InetSocketAddress BROADCAST_ADDRESS =
			new InetSocketAddress("255.255.255.255", BROADCAST_PORT);
	
	private Object androidContext;
	
	private EventBus bus;
	
	private DatagramChannel channel;
	private Thread listenerThread;
	private Thread broadcastThread;
	
	/**
	 * Creates a new BroadcastListener using the given android context. If the
	 * platform is not android, this context may be left {@code null}.
	 * @param androidContext the current Android context, or null
	 */
	public BroadcastListener(Object androidContext) {
		this.androidContext = androidContext;
		
		bus = new EventBus() {{
			add(GatewayDiscoveredEvent.class);
			add(PacketReceivedEvent.class);
		}};
	}
	
	/**
	 * Creates a new BroadcastListener. If using android, an android context
	 * must be provided to enable multicast; see
	 * {@link #BroadcastListener(java.lang.Object)}.
	 */
	public BroadcastListener() {
		this(null);
	}
	
	@Override
	public EventBusClient bus() {
		return bus.getClient();
	}
	
	/**
	 * Begins listening for UDP broadcasts on the {@link #BROADCAST_PORT}.
	 * @param daemon if true, threads are spawned in daemon mode and will allow
	 *     program termination with the main thread
	 * @throws IOException on network error
	 */
	public void startListen(boolean daemon) throws IOException {
		if (isListening() ||
				(listenerThread != null && listenerThread.isAlive())) {
			log.debug("Attempted to spawn multiple listener threads,"
					+ " ignoring...");
			return;
		}
		
		channel = DatagramChannel.open();
		channel.socket().bind(new InetSocketAddress(BROADCAST_PORT));
		channel.socket().setBroadcast(true);
		channel.configureBlocking(true);
		
		listenerThread = new Thread(listener, "lifx-udp-listen");
		listenerThread.setDaemon(daemon);
		listenerThread.start();
		
		broadcastThread = new Thread(broadcaster, "lifx-udp-broadcast");
		broadcastThread.setDaemon(daemon);
		broadcastThread.start();
		
		log.debug("Started listening on port " + BROADCAST_PORT);
	}
	
	/**
	 * Begins listening for UDP broadcasts on {@link #BROADCAST_PORT}. By
	 * default, listening threads are not spawned in daemon mode; see
	 * {@link #startListen(boolean)} to configure this.
	 * @throws IOException on network error
	 */
	public void startListen() throws IOException {
		startListen(false);
	}
	
	/**
	 * Stops broadcast packets for gateway discovery. This can be called to
	 * stop searching for bulbs while keeping the channel open to communicate
	 * with gateways already known.
	 * @throws IOException on network error
	 */
	public void stopDiscovery() throws IOException {
		broadcastThread.interrupt();
	}
	
	/**
	 * Closes the UDP channel and stops listening and broadcast threads.
	 * Depending on the platform, it may be important to close the channel
	 * whenever possible to reduce contention on the port. Particularly on
	 * Android, other apps will try to bind to it.
	 * @throws IOException on network error
	 */
	public void stopListen() throws IOException {
		if (listenerThread == null) {
			return;
		}
		
		channel.close();
		
		log.debug("Listening stopped");
	}
	
	/**
	 * Returns true currently listening for UDP packets
	 * @return true if listening, false if not
	 */
	public boolean isListening() {
		return channel != null && channel.isOpen();
	}
	
	/**
	 * Sends the given packet to the specified destination.
	 * @param packet the packet to send
	 * @param destination the destination address for the packet
	 * @throws java.nio.channels.ClosedChannelException
	 * @throws java.io.IOException
	 */
	public void send(Packet packet, InetSocketAddress destination)
			throws ClosedChannelException, IOException {
		channel.send(packet.bytes(), destination);
	}
	
	/**
	 * Broadcasts the given packet to all possible addresses.
	 * @param packet the packet to broadcast
	 * @throws java.nio.channels.ClosedChannelException
	 * @throws java.io.IOException
	 */
	public void broadcast(Packet packet)
			throws ClosedChannelException, IOException {
		channel.send(packet.bytes(), BROADCAST_ADDRESS);
	}
	
	private final Runnable listener = new Runnable() {

		@Override
		public void run() {
			GatewayManager manager = GatewayManager.getInstance();
			while (true) {
				try {
					ByteBuffer buf = ByteBuffer.allocate(512);
					InetSocketAddress a = (InetSocketAddress) channel.receive(buf);
	
					buf.rewind();
					
					// extract the packet type field manually
					ByteBuffer packetType = buf.slice();
					packetType.position(32);
					packetType.limit(34);
					
					int type = Packet.FIELD_PACKET_TYPE.value(packetType);
					
					log.trace(
							"Packet type {} received",
							String.format("0x%02X", type));
					
					// we only handle gateway messages here (0x03), so we don't
					// need to use the PacketFactory
					if (type == 0x03) {
						PANGatewayResponse packet = new PANGatewayResponse();
						packet.parse(buf);
						
						int port = (int) packet.getPort();
						if (port == 0) {
							// we get port 0 responses for some reason (?)
							// just ignore them
							continue;
						}
						
						if (!manager.hasGateway(a, port)) {
							Gateway g = new Gateway(
									BroadcastListener.this, a,
									port, packet.getSite());
							manager.registerGateway(g);
							
							log.debug("Gateway found: {}", g);
							
							bus.push(new GatewayDiscoveredEvent(g));
						} else {
							log.trace("Existing gateway found.");
						}
					} else {
						// attempt to parse the packet
						PacketHandler handler = PacketFactory.createHandler(type);
						if (handler == null) {
							log.trace("Unknown packet type: {}",
									String.format("0x%02X", type));
							continue;
						}

						Packet packet = handler.handle(buf);
						if (packet == null) {
							log.warn("Handler {} was unable to handle packet",
									handler.getClass().getName());
							continue;
						}
						
						log.debug("Dispatching packet: {}", packet);
						
						bus.push(new PacketReceivedEvent(
								BroadcastListener.this, a, packet));
					}
					
					//PacketHandler h = PacketFactory.createHandler(type);
					//if (h == null) {
					//	log.info("Unknown packet from {}, type: {}",
					//			a,
					//			Integer.toHexString(type));
					//} else {
					//	log.info("Packet: {}", h.handle(buf));
					//}
				} catch (ClosedChannelException ex) {
					// thread killed
					log.debug("Listener ended");
					break;
				} catch (IOException ex) {
					log.error("Error while receiving packet", ex);
				}
			}
		}
		
	};
	
	private final Runnable broadcaster = new Runnable() {

		@Override
		public void run() {
			PANGatewayRequest r = new PANGatewayRequest();
			
			// android requires a multicast lock, so we use a proxy class
			// if the WifiManager is missing (i.e. not android) it does nothing
			
			WifiManagerProxy wifiManager = WifiManagerProxy.getInstance(androidContext);
			MulticastLockProxy lock = wifiManager.createMulticastLock(
					getClass().getPackage().getName());
			lock.acquire();
			
			while (true) {
				try {
					broadcast(r);
					log.trace("Discovery packet sent");
				} catch (ClosedChannelException ex) {
					break;
				} catch (Exception ex) {
					log.error("Error sending discovery packet", ex);
				}
				
				try {
					Thread.sleep(BROADCAST_DELAY);
				} catch (InterruptedException ex) {
					break;
				}
			}
			
			lock.release();
		}
		
	};
	
}
