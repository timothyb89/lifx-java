package org.timothyb89.lifx.net;

import org.timothyb89.lifx.gateway.GatewayManager;
import org.timothyb89.lifx.gateway.Gateway;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.lifx.net.android.WifiManagerProxy;
import org.timothyb89.lifx.net.android.WifiManagerProxy.MulticastLockProxy;
import org.timothyb89.lifx.net.packet.Packet;
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
	 * Begins listening for UDP broadcasts on {@link #BROADCAST_PORT}.
	 * @param daemon if true, threads are spawned in daemon mode and will allow
	 *     program termination with the main thread
	 * @throws IOException 
	 */
	public void startListen(boolean daemon) throws IOException {
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
	 * @throws IOException 
	 */
	public void startListen() throws IOException {
		startListen(false);
	}
	
	/**
	 * Closes the UDP channel and stops listening and broadcast threads.
	 * @throws IOException 
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
	
	private final Runnable listener = new Runnable() {

		@Override
		public void run() {
			GatewayManager manager = GatewayManager.getInstance();
			while (true) {
				try {
					ByteBuffer buf = ByteBuffer.allocate(512);
					InetSocketAddress a = (InetSocketAddress) channel.receive(buf);
	
					buf.rewind();
					
					ByteBuffer packetType = buf.slice();
					packetType.position(32);
					packetType.limit(34);
					
					int type = Packet.FIELD_PACKET_TYPE.value(packetType);
					
					// we only handle gateway messages here (0x03), so we don't
					// need to use the PacketFactory
					if (type == 0x03) {
						PANGatewayResponse packet = new PANGatewayResponse();
						packet.parse(buf);
						
						int port = (int) packet.getPort();
						
						if (!manager.hasGateway(a, port)) {
							Gateway g = new Gateway(a, port, packet.getSite());
							manager.registerGateway(g);
							
							log.debug("Gateway found: {}", g);
							
							bus.push(new GatewayDiscoveredEvent(g));
						} else {
							log.debug("Existing gateway found.");
						}
					} else {
						log.debug("Unknown UDP packet received: " + type);
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
			InetSocketAddress a = new InetSocketAddress(
					"255.255.255.255", BROADCAST_PORT);
			
			// android requires a multicast lock, so we use a proxy class
			// if the WifiManager is missing (i.e. not android) it does nothing
			
			WifiManagerProxy wifiManager = WifiManagerProxy.getInstance(androidContext);
			MulticastLockProxy lock = wifiManager.createMulticastLock(
					getClass().getPackage().getName());
			lock.acquire();
			
			while (true) {
				try {
					channel.send(r.bytes(), a);
					log.debug("discovery packet sent");
					
					Thread.sleep(BROADCAST_DELAY);
				} catch (ClosedChannelException | InterruptedException ex) {
					break;
				} catch (Exception ex) {
					log.error("Error sending discovery packet", ex);
				}
			}
			
			lock.release();
		}
		
	};
	
}
