package org.timothyb89.lifx.bulb;

import java.io.IOException;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.eventbus.EventHandler;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.GatewayPacketReceivedEvent;
import org.timothyb89.lifx.gateway.PacketResponseFuture;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.request.SetLightColorRequest;
import org.timothyb89.lifx.net.packet.request.SetPowerStateRequest;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;
import org.timothyb89.lifx.net.packet.response.PowerStateResponse;

/**
 * Manages interactions directly with LIFX bulbs, and provides some abstractions
 * for setting various properties of bulbs, like color, brightness, power, and
 * so on.
 * @author timothyb89
 */
@ToString(of = { "label", "address", "powerState" })
public class Bulb implements EventBusProvider {
	
	public static final long DEFAULT_FADE_TIME = 1000l;
	
	@Getter private final Gateway gateway;
	@Getter private final MACAddress address;
	@Getter private LIFXColor color;
	@Getter private int dim;
	@Getter private PowerState powerState;
	@Getter private String label;
	@Getter private long tags;

	private final EventBus bus;
	
	public Bulb(Gateway gateway, MACAddress address) {
		this.gateway = gateway;
		this.address = address;
		
		gateway.bus().register(this);
		
		bus = new EventBus() {{
			add(BulbPowerStateUpdatedEvent.class);
			add(BulbStatusUpdatedEvent.class);
		}};
	}

	@Override
	public EventBusClient bus() {
		return bus.getClient();
	}
	
	/**
	 * Updates this bulb with information from the given packet. This particular
	 * method will trigger a {@link BulbStatusUpdatedEvent}.
	 * @param packet the packet to update from
	 */
	public void valuesFromPacket(LightStatusResponse packet) {
		color = new LIFXColor(packet);
		dim = packet.getDim();
		label = packet.getLabel();
		tags = packet.getTags();
		
		if (packet.getPower() != null) {
			// for whatever reason, power state is occasionally null, i.e. it
			// has some invalid (+unknown) value
			// this seems to correct itself on the next update, but to avoid
			// propagating a known-bad value we'll ignore it for now
			// TODO: figure out what's up with this value and actually correct
			// it
			// see also: PowerState.fromValue(), where this originates (on our
			// end)
			powerState = packet.getPower();
		}
		
		bus.push(new BulbStatusUpdatedEvent(
				this, powerState, color, dim, label, tags));
	}
	
	/**
	 * Updates this bulb with information from the given packet. This particular
	 * method will trigger a {@link BulbPowerStateUpdatedEvent}.
	 * @param packet the packet to update from
	 */
	public void valuesFromPacket(PowerStateResponse packet) {
		powerState = packet.getState();
		
		bus.push(new BulbPowerStateUpdatedEvent(this, powerState));
	}
	
	/**
	 * Sends a packet to this bulb. Note that the {@code site} field will be set
	 * to the address of this bulb. See {@link Gateway#send(Packet)} for more
	 * information on this behavior.
	 * @param packet the packet to send
	 * @throws IOException on network error
	 * @return a {@link PacketResponseFuture}
	 */
	public PacketResponseFuture send(Packet packet) throws IOException {
		packet.setBulbAddress(address);
		packet.setSite(gateway.getMacAddress());
		return gateway.sendRaw(packet);
	}
	
	/**
	 * Sets the power state of this bulb. A {@link SetPowerStateRequest} will
	 * be sent to this bulb. Note that the state will not be updated
	 * immediately; a {@link PowerStateResponse} or a
	 * {@link LightStatusResponse} must be received first.
	 * @param state the new state to set
	 * @throws IOException on network error
	 */
	public void setPowerState(PowerState state) throws IOException {
		send(new SetPowerStateRequest(state));
	}
	
	/**
	 * Turns off this bulb.
	 * @see #setPowerState(PowerState) 
	 * @throws IOException on network error
	 */
	public void turnOff() throws IOException {
		setPowerState(PowerState.OFF);
	}
	
	/**
	 * Turns on this bulb.
	 * @see #setPowerState(PowerState) 
	 * @throws IOException on network error
	 */
	public void turnOn() throws IOException {
		setPowerState(PowerState.ON);
	}
	
	/**
	 * Attempts to set the color of this bulb. 
	 * @param color the color to set
	 * @param fadeTime bulb fade time, appears to be in milliseconds,
	 *     sometimes (??)
	 * @throws IOException on network error
	 */
	public void setColor(LIFXColor color, long fadeTime) throws IOException {
		send(new SetLightColorRequest(color, fadeTime));
	}
	
	/**
	 * Attempts to set the color of this bulb. A default fade time of
	 * {@code 1000} is used.
	 * @see #setColor(LIFXColor, long) 
	 * @param color the color to set
	 * @throws IOException on network error
	 */
	public void setColor(LIFXColor color) throws IOException {
		setColor(color, DEFAULT_FADE_TIME);
	}
	
	@EventHandler
	public void packetReceived(GatewayPacketReceivedEvent event) {
		Packet p = event.getPacket();
		
		// make sure the packet is for this bulb
		if (!p.getBulbAddress().equals(address)) {
			return;
		}
		
		if (p instanceof PowerStateResponse) {
			PowerStateResponse resp = (PowerStateResponse) p;
			valuesFromPacket(resp);
		} else if (p instanceof LightStatusResponse) {
			LightStatusResponse resp = (LightStatusResponse) p;
			valuesFromPacket(resp);
		}
		
		// TODO implement events + updates
	}
	
}
