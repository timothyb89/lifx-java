package org.timothyb89.lifx.net.packet;

import java.util.HashMap;
import java.util.Map;
import org.timothyb89.lifx.net.packet.response.*;
import org.timothyb89.lifx.net.packet.handler.*;

/**
 * A static factory for registering packet types that may be received and
 * dispatched to client cod * request types, like {@code PowerStateRequest}) or types received only via UDP
e. Packet handlers (used to construct actual packet
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
		handlers = new HashMap<>();
		
		register(PowerStateResponse.class);
		register(LightStatusResponse.class);
		register(BulbLabelResponse.class);
		register(MeshFirmwareResponse.class);
		register(TagLabelsResponse.class);
		register(TagsResponse.class);
		register(WifiInfoResponse.class);
	}
	
	/**
	 * Registers a packet handler for the given packet type.
	 * @param type the type to register
	 * @param handler the packet handler to associate with the type
	 */
	public final void register(int type, PacketHandler handler) {
		handlers.put(type, handler);
	}
	
	/**
	 * Registers a new generic packet handler for the given packet class. The
	 * packet class must meet the criteria for {@link GenericHandler};
	 * specifically, it must have an no-argument constructor and require no
	 * parsing logic outside of an invocation of
	 * {@link Packet#parse(java.nio.ByteBuffer)}.
	 * @param type the type of the packet to register
	 * @param clazz the class of the packet to register
	 */
	public final void register(int type, Class<? extends Packet> clazz) {
		handlers.put(type, new GenericHandler(clazz));
	}
	
	/**
	 * Registers a generic packet type. All requirements of
	 * {@link GenericHandler} must met; specifically, classes must have a
	 * no-args constructor and require no additional parsing logic.
	 * Additionally, a public static integer {@code TYPE} field must be defined.
	 * @param <T> the packet type to register
	 * @param clazz the packet class to register
	 */
	public final <T extends Packet> void register(Class<T> clazz) {
		GenericHandler<T> handler = new GenericHandler(clazz);
		
		if (!handler.isTypeFound()) {
			throw new IllegalArgumentException(
					"Unable to register generic packet with no TYPE field.");
		}
		
		handlers.put(handler.getType(), handler);
	}
	
	/**
	 * Gets a registered handler for the given packet type, if any exists. If
	 * no matching handler can be found, {@code null} is returned.
	 * @param packetType the packet type of the handler to retrieve
	 * @return a packet handler, or null
	 */
	public PacketHandler getHandler(int packetType) {
		return handlers.get(packetType);
	}
	
	public static PacketHandler createHandler(int packetType) {
		return getInstance().getHandler(packetType);
	}
	
}
