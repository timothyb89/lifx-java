package org.timothyb89.lifx.net.packet.handler;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;

/**
 * A packet handler responsible for converting a ByteBuffer into a Packet
 * instance.
 * @author timothyb89
 * @param <T>
 */
public interface PacketHandler<T extends Packet> {
	
	public abstract T handle(ByteBuffer buf);
	
}
