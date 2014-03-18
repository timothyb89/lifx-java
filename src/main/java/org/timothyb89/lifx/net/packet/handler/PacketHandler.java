package org.timothyb89.lifx.net.packet.handler;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public interface PacketHandler {
	
	public abstract Packet handle(ByteBuffer buf);
	
}
