package org.timothyb89.lifx.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
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
import org.timothyb89.eventbus.EventScanMode;
import org.timothyb89.eventbus.EventScanType;
import org.timothyb89.lifx.bulb.Bulb;
import org.timothyb89.lifx.bulb.PowerState;
import org.timothyb89.lifx.net.BroadcastListener;
import org.timothyb89.lifx.net.PacketReceivedEvent;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.request.LightStatusRequest;
import org.timothyb89.lifx.net.packet.request.SetPowerStateRequest;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;

/**
 * Defines a basic gateway. This handles interactions with a gateway
 * bulb which then dispatches commands to its connected bulbs (or potentially
 * itself).
 * @author timothyb89
 */
@Slf4j
@ToString(of = { "ipAddress", "port", "macAddress", "bulbs" })
@EventScanMode(type = EventScanType.EXTENDED)
public class Gateway implements EventBusProvider {
	
	@Getter private final BroadcastListener listener;
	@Getter private final InetSocketAddress ipAddress;
	@Getter private final int port;
	@Getter private final MACAddress macAddress;

	private final Deque<PacketResponseFuture> responses;
	
	private final EventBus bus;
	
	private List<Bulb> bulbs;

	/**
	 * Creates a new Gateway instance. This should generally only be called by
	 * BroadcastListener.
	 * @param listener a BroadcastListener instance, used for packet IO
	 * @param ipAddress the IP address of this gateway
	 * @param port the port used for IO
	 * @param macAddress the MAC address of this gateway
	 */
	public Gateway(
			BroadcastListener listener, InetSocketAddress ipAddress,
			int port, MACAddress macAddress) {
		this.listener = listener;
		this.ipAddress = ipAddress;
		this.port = port;
		this.macAddress = macAddress;
		
		responses = new ConcurrentLinkedDeque<>();
		
		bulbs = Collections.synchronizedList(new LinkedList<Bulb>());
		
		bus = new EventBus() {{
			add(GatewayPacketSentEvent.class);
			add(GatewayPacketReceivedEvent.class);
			add(GatewayBulbDiscoveredEvent.class);
		}};
		
		listener.bus().register(this);
		bus.register(this);
		
		// there's no connection process, so immediately start bulb discovery
		try {
			refreshBulbs();
		} catch (IOException ex) {
			log.warn("Unable to query gateway for bulbs", ex);
		}
	}

	@Override
	public EventBusClient bus() {
		return bus.getClient();
	}
	
	/**
	 * Returns true if a socket is open for this gateway. Note that as of the
	 * v1.2 firmware update all communication is done over UDP which changes the
	 * semantics of this method. Currently this returns whether or not the
	 * BroadcastListener is actually listening in lieu of a TCP connection.
	 * @return true if currently able to communicate with bulb, false if not
	 */
	public boolean isConnected() {
		//return channel != null && channel.isConnected();
		// apparently channel.isConnected() does not actually tell you if the
		// socket is connected - at least on android
		// we do get usable EOFs, so we'll keep track ourselves
		return listener.isListening();
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
	 * @throws IOException on network error
	 */
	public PacketResponseFuture sendRaw(Packet packet) throws IOException {
		PacketResponseFuture f = new PacketResponseFuture(packet);
		responses.offer(f);
		
		listener.send(packet, ipAddress);
		
		bus.push(new GatewayPacketSentEvent(this, packet, f));
		
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
	 * @throws IOException on network error
	 */
	public Future<PacketResponse> send(Packet packet) throws IOException {
		packet.setSite(macAddress);
		return sendRaw(packet);
	}
	
	/**
	 * Returns a list of all bulbs connected to this gateway. The returned is
	 * unmodifiable.
	 * @return a list of known bulbs
	 */
	public List<Bulb> getBulbs() {
		return Collections.unmodifiableList(bulbs);
	}
	
	/**
	 * Gets the bulb with the given MAC address, if any matching bulb exists. If
	 * no such bulb can be found, {@code null} is returned.
	 * @param address the address of the bulb to search for
	 * @return the bulb with the given address, or {@code null}
	 */
	public Bulb getBulb(MACAddress address) {
		for (Bulb b : bulbs) {
			if (b.getAddress().getHex().equalsIgnoreCase(address.getHex())) {
				return b;
			}
		}
		
		return null;
	}
	
	/**
	 * Refreshes bulb state by issuing a {@link LightStatusRequest} to all bulbs
	 * connected to this gateway. Note that this may trigger some number of
	 * {@link GatewayBulbDiscoveredEvent}s for any new bulb discovered.
	 * @throws IOException on network error
	 */
	public void refreshBulbs() throws IOException {
		send(new LightStatusRequest());
	}
	
	/**
	 * Sets the power state of all bulbs connected to this gateway. A
	 * {@link SetPowerStateRequest} will be sent directly to the gateway.
	 * @param state the power state to set
	 * @throws IOException on network error
	 */
	public void setPowerState(PowerState state) throws IOException {
		send(new SetPowerStateRequest(state));
	}
	
	/**
	 * Turns off all bulbs connected to this gateway.
	 * @see #setPowerState(PowerState) 
	 * @throws IOException on network error
	 */
	public void turnOff() throws IOException {
		setPowerState(PowerState.OFF);
	}
	
	/**
	 * Turns on all bulbs connected to this gateway.
	 * @see #setPowerState(PowerState) 
	 * @throws IOException on network error
	 */
	public void turnOn() throws IOException {
		setPowerState(PowerState.ON);
	}
		
	/**
	 * Called when the BroadcastListener has received a packet.
	 * @param event 
	 */
	@EventHandler
	private void onPacketReceived(PacketReceivedEvent event) {
		if (!event.getSource().equals(ipAddress)) {
			// ignore packets from other gateways
			return;
		}
		
		Packet packet = event.getPacket();
		int type = packet.getPacketType();
		
		log.debug("Packet {} for gateway {}", packet, this);
		
		bus.push(new GatewayPacketReceivedEvent(this, packet));
					
		// clean up fulfilled (empty) response futures
		List<PacketResponseFuture> toRemove = new LinkedList<>();

		// is this a response?
		PacketResponseFuture recipient = null;
		for (PacketResponseFuture f : responses) {
			if (f.isFulfilled()) {
				toRemove.add(f);
				continue;
			}

			if (recipient == null
					&& f.expectsResponse(type, packet.getBulbAddress())) {

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
		
		// check for a bulb discovery response
		if (packet instanceof LightStatusResponse) {
			LightStatusResponse resp = (LightStatusResponse) packet;
			
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
	
}
