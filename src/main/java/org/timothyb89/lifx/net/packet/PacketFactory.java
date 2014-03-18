package org.timothyb89.lifx.net.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.timothyb89.lifx.net.packet.handler.IgnoreHandler;
import org.timothyb89.lifx.net.packet.handler.PANGatewayResponseHandler;
import org.timothyb89.lifx.net.packet.handler.PacketHandler;

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
			put(0x02, new IgnoreHandler());
			put(0x03, new PANGatewayResponseHandler());
		}};
	}
	
	public PacketHandler getHandler(int packetType) {
		return handlers.get(packetType);
	}
	
	public static PacketHandler createHandler(int packetType) {
		return getInstance().getHandler(packetType);
	}
	
}
