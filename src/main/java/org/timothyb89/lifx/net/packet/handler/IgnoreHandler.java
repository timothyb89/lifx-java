package org.timothyb89.lifx.net.packet.handler;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.GenericPacket;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class IgnoreHandler implements PacketHandler {

	@Override
	public Packet handle(ByteBuffer buf) {
		GenericPacket p = new GenericPacket();
		p.parse(buf);
		return p;
	}
	
}
