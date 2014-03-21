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
import org.timothyb89.lifx.net.packet.request.SetPowerStateRequest;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;
import org.timothyb89.lifx.net.packet.response.PowerStateResponse;

/**
 *
 * @author tim
 */
@ToString(of = { "label", "address", "powerState" })
public class Bulb implements EventBusProvider {
	
	@Getter private Gateway gateway;
	@Getter private MACAddress address;
	@Getter private int hue;
	@Getter private int saturation;
	@Getter private int brightness;
	@Getter private int dim;
	@Getter private PowerState powerState;
	@Getter private String label;
	@Getter private long tags;

	private EventBus bus;
	
	public Bulb(Gateway gateway, MACAddress address) {
		this.gateway = gateway;
		this.address = address;
		
		gateway.bus().register(this);
		
		bus = new EventBus() {{
			add(BulbStatusUpdatedEvent.class);
		}};
	}

	@Override
	public EventBusClient bus() {
		return bus.getClient();
	}
	
	public void valuesFromPacket(LightStatusResponse packet) {
		hue = packet.getHue();
		saturation = packet.getSaturation();
		brightness = packet.getBrightness();
		dim = packet.getDim();
		label = packet.getLabel();
		tags = packet.getTags();
		
		powerState = packet.getPower();
	}
	
	/**
	 * Sends a packet to this bulb. Note that the {@code site} field will be set
	 * to the address of this bulb. See {@link Gateway#send(Packet)} for more
	 * information on this behavior.
	 * @param packet the packet to send
	 * @throws IOException
	 * @return a {@link PacketResponseFuture}
	 */
	public PacketResponseFuture send(Packet packet) throws IOException {
		packet.setSite(address);
		return gateway.sendRaw(packet);
	}
	
	/**
	 * Sets the power state of this bulb. A {@link SetPowerStateRequest} will
	 * be sent to this bulb. Note that the state will not be updated
	 * immediately; a {@link PowerStateResponse} or a
	 * {@link LightStatusResponse} must be received first.
	 * @param state the new state to set
	 * @throws java.io.IOException
	 */
	public void setPowerState(PowerState state) throws IOException {
		send(new SetPowerStateRequest(state));
	}
	
	/**
	 * Turns off this bulb.
	 * @see #setPowerState(PowerState) 
	 * @throws java.io.IOException
	 */
	public void turnOff() throws IOException {
		setPowerState(PowerState.OFF);
	}
	
	/**
	 * Turns on this bulb.
	 * @see #setPowerState(PowerState) 
	 * @throws IOException 
	 */
	public void turnOn() throws IOException {
		setPowerState(PowerState.ON);
	}
	
	@EventHandler
	public void packetReceived(GatewayPacketReceivedEvent event) {
		Packet p = event.getPacket();
		
		if (p instanceof PowerStateResponse) {
			PowerStateResponse resp = (PowerStateResponse) p;
			
		} else if (p instanceof LightStatusResponse) {
			
		}
		
		// TODO implement events + updates
	}
	
}
