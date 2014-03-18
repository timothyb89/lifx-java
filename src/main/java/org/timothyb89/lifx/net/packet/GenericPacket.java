package org.timothyb89.lifx.net.packet;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class GenericPacket extends Packet {

	@Override
	public int packetType() {
		return 0;
	}

	@Override
	protected int packetLength() {
		return 0;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(0);
	}
	
}
