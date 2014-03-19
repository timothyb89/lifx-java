package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.response.MeshFirmwareResponse;

/**
 *
 * @author tim
 */
public class MeshFirmwareRequest extends Packet {

	public static final int TYPE = 0x0E;
	
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
		return new int[] {
			MeshFirmwareResponse.TYPE
		};
	}
	
}
