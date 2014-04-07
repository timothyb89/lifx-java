package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.UInt64Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class GetTagLabelsRequest extends Packet {

	public static final int TYPE = 0x1D;
	
	public static final Field<Long> FIELD_TAGS = new UInt64Field();
	
	@Getter @Setter private long tags;

	public GetTagLabelsRequest() {
	}
	
	public GetTagLabelsRequest(long tags) {
		this.tags = tags;
	}
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 8;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		tags = FIELD_TAGS.value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_TAGS.bytes(tags));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
