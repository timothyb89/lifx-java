package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import lombok.ToString;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@ToString(callSuper = true)
public class PANGatewayRequest extends Packet {

	public static final int TYPE = 0x02;
	
	//public static final int PROTOCOL_DEFAULT = 21504; // ??
	
	public PANGatewayRequest() {
		// v2.0 protocol uses default protocol in Packet
		//protocol = PROTOCOL_DEFAULT;
	}
	
	@Override
	public int packetLength() {
		return 0;
	}

	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		// empty
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(0);
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {}; // UDP packets cannot have responses
	}
	
}
