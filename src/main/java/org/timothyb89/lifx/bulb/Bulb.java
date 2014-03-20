package org.timothyb89.lifx.bulb;

import lombok.Getter;
import lombok.ToString;
import org.timothyb89.eventbus.EventBus;
import org.timothyb89.eventbus.EventBusClient;
import org.timothyb89.eventbus.EventBusProvider;
import org.timothyb89.eventbus.EventHandler;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.GatewayPacketReceivedEvent;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;
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
	
	@EventHandler
	public void packetReceived(GatewayPacketReceivedEvent event) {
		Packet p = event.getPacket();
		
		if (p instanceof PowerStateResponse) {
			PowerStateResponse resp = (PowerStateResponse) p;
			
		} else if (p instanceof LightStatusResponse) {
			
		}
	}
	
}
