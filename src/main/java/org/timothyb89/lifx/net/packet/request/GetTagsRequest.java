package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class GetTagsRequest extends Packet {

	public static final int TYPE = 0x1A;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 0;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		// do nothing
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(0);
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
