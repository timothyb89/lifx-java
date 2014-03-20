package org.timothyb89.lifx.net.packet;

import java.util.HashMap;
import java.util.Map;
import org.timothyb89.lifx.net.packet.handler.LightStatusResponseHandler;
import org.timothyb89.lifx.net.packet.handler.PacketHandler;
import org.timothyb89.lifx.net.packet.handler.PowerStateResponseHandler;

/**
 *
 * @author tim
 */
public class PacketFactory {
	
	private static PacketFactory instance;

	public synchronized static PacketFactory getInstance() {
		if (instance == null) {
			instance = new PacketFactory();
		}
		
		return instance;
	}
	
	private Map<Integer, PacketHandler> handlers;
	
	private PacketFactory() {
		handlers = new HashMap<Integer, PacketHandler>() {{
			//put(0x02, new IgnoreHandler());
			//put(0x03, new PANGatewayResponseHandler());
			put(0x16, new PowerStateResponseHandler());
			put(0x6B, new LightStatusResponseHandler());
		}};
	}
	
	public PacketHandler getHandler(int packetType) {
		return handlers.get(packetType);
	}
	
	public static PacketHandler createHandler(int packetType) {
		return getInstance().getHandler(packetType);
	}
	
}
