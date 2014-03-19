package org.timothyb89.lifx.net;

import java.io.IOException;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.timothyb89.eventbus.EventHandler;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.PacketResponse;
import org.timothyb89.lifx.net.packet.request.PowerStateRequest;
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
	public void gatewayFound(GatewayFoundEvent ev) {
		try {
			listener.stopListen(); // no need to spam
		} catch (IOException ex) {
			// om nom nom
		}
		
		Gateway g = ev.getGateway();
		
		log.info("Got gateway: {}", ev.getGateway());
		
		try {
			g.connect();
			
			Future<PacketResponse> respFuture = g.send(new PowerStateRequest());
			PacketResponse resp = respFuture.get();
			
			PowerStateResponse psr = resp.get(PowerStateResponse.class);
			
			System.out.println(resp);
			
			System.out.println(psr.getState());
		} catch (Exception ex) {
			log.error("couldn't connect", ex);
		}
	}
	
	public static void main(String[] args) throws IOException {
		new TestClient();
	}
	
}
