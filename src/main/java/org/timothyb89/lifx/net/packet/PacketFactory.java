package org.timothyb89.lifx.net.packet;

import java.util.HashMap;
import java.util.Map;
import org.timothyb89.lifx.net.packet.response.*;
import org.timothyb89.lifx.net.packet.handler.*;

/**
 * A static factory for registering packet types that may be received and
 * dispatched to client code. Packet handlers (used to construct actual packet
 * instances) may be retrieved via their packet type.
 * 
 * <p>This factory does not handle packet types used only for sending (most
 * request types, like {@code PowerStateRequest}) or types received only via UDP
 * (like {@code PANGatewayResponse}).</p>
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
	
	private final Map<Integer, PacketHandler> handlers;
	
	private PacketFactory() {
		handlers = new HashMap<Integer, PacketHandler>() {{
			//put(0x02, new IgnoreHandler());
			//put(0x03, new PANGatewayResponseHandler());
			put(PowerStateResponse.TYPE,  new PowerStateResponseHandler());
			put(LightStatusResponse.TYPE, new LightStatusResponseHandler());
		}};
	}
	
	public PacketHandler getHandler(int packetType) {
		return handlers.get(packetType);
	}
	
	public static PacketHandler createHandler(int packetType) {
		return getInstance().getHandler(packetType);
	}
	
}
