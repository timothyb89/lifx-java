package org.timothyb89.lifx.net;

import org.timothyb89.lifx.gateway.GatewayManager;
import org.timothyb89.lifx.gateway.Gateway;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.lifx.net.android.WifiManagerProxy;
import org.timothyb89.lifx.net.android.WifiManagerProxy.MulticastLockProxy;
import org.timothyb89.lifx.net.packet.ByteBufferTest;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.PacketFactory;
import org.timothyb89.lifx.net.packet.handler.PacketHandler;
import org.timothyb89.lifx.net.packet.request.PANGatewayRequest;
import org.timothyb89.lifx.net.packet.response.PANGatewayResponse;

/**
 * Listens for UDP broadcasts from gateway bulbs.
 * @author tim
 */
@Slf4j
public class BroadcastListener implements EventBusProvider {
	
	public static final int BROADCAST_PORT = 56700;
	public static final int BROADCAST_DELAY = 1000;
	
	private EventBus bus;
	
	private DatagramChannel channel;
	private Thread listenerThread;
	private Thread broadcastThread;
	
	public BroadcastListener() {
		bus = new EventBus() {{
			add(GatewayFoundEvent.class);
		}};
	}
	
	@Override
	public EventBusClient bus() {
		return bus.getClient();
	}
	
	public void startListen(boolean daemon) throws IOException {
		channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(BROADCAST_PORT));
		channel.socket().setBroadcast(true);
		channel.configureBlocking(true);
		
		listenerThread = new Thread(listener, "lifx-udp-listen");
		listenerThread.setDaemon(daemon);
		listenerThread.start();
		
		broadcastThread = new Thread(broadcaster, "lifx-udp-broadcast");
		broadcastThread.setDaemon(daemon);
		broadcastThread.start();
		
		log.info("Started listening on port " + BROADCAST_PORT);
	}
	
	public void startListen() throws IOException {
		startListen(false);
	}
	
	public void stopListen() throws IOException {
		if (listenerThread == null) {
			return;
		}
		
		channel.close();
		
		log.info("Listening stopped");
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
							
							log.info("Gateway found: {}", g);
							
							bus.push(new GatewayFoundEvent(g));
						} else {
							log.info("Existing gateway found.");
						}
					} else {
						log.warn("Unknown UDP packet received: " + type);
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
					log.info("Listener ended");
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
			
			WifiManagerProxy wifiManager = WifiManagerProxy.getInstance();
			MulticastLockProxy lock = wifiManager.createMulticastLock(
					getClass().getPackage().getName());
			lock.acquire();
			
			while (true) {
				try {
					channel.send(r.bytes(), a);
					log.info("discovery packet sent");
					
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
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		BroadcastListener l = new BroadcastListener();
		l.startListen();
		
		System.out.println("listening...");
		
		Thread.sleep(10000);
		
		l.stopListen();
		
		System.out.println("killed");
	}
	
}
