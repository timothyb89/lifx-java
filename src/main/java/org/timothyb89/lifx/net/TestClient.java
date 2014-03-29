package org.timothyb89.lifx.net;

import java.io.IOException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventHandler;
import org.timothyb89.lifx.bulb.Bulb;
import org.timothyb89.lifx.bulb.LIFXColor;
import org.timothyb89.lifx.bulb.PowerState;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.GatewayBulbDiscoveredEvent;
import org.timothyb89.lifx.gateway.GatewayPacketReceivedEvent;
import org.timothyb89.lifx.gateway.PacketResponse;
import org.timothyb89.lifx.net.packet.request.PowerStateRequest;
import org.timothyb89.lifx.net.packet.request.SetPowerStateRequest;
import org.timothyb89.lifx.net.packet.response.PowerStateResponse;

/**
 *
 * @author tim
 */
@Slf4j
public class TestClient {

	private BroadcastListener listener;
	
	public TestClient() throws IOException {
		listener = new BroadcastListener();
		listener.bus().register(this);
		listener.startListen();
	}
	
	@EventHandler
	public void gatewayFound(GatewayDiscoveredEvent ev) {
		try {
			listener.stopListen(); // no need to spam
		} catch (IOException ex) {
			// om nom nom
		}
		
		Gateway g = ev.getGateway();
		g.bus().register(this);
		
		System.out.println("Got gateway: " + ev.getGateway());
		
		try {
			g.connect();
			
			//g.send(new PowerStateRequest());
			//g.send(new SetPowerStateRequest(PowerState.ON));
		} catch (Exception ex) {
			log.error("couldn't connect", ex);
		}
	}
	
	@EventHandler
	public void bulbDiscovered(GatewayBulbDiscoveredEvent event) throws IOException {
		System.out.println("Bulb found: " + event.getBulb());
		Bulb b = event.getBulb();
		
		LIFXColor color = LIFXColor.fromRGB(127, 127, 0);
		b.setColor(color);
		
		b.getGateway().disconnect();
	}
	
	@EventHandler
	public void packetReceived(GatewayPacketReceivedEvent event) {
		log.info("<---- {}", event.getPacket());
	}
	
	public static void main(String[] args) throws IOException {
		new TestClient();
	}
	
}
