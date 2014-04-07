package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.StringField;
import org.timothyb89.lifx.net.field.UInt64Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class TagLabelsResponse extends Packet {

	public static final int TYPE = 0x1F;
	
	public static final Field<Long>   FIELD_TAGS  = new UInt64Field();
	public static final Field<String> FIELD_LABEL = new StringField(32).utf8();
	
	@Getter private long tags;
	@Getter private String label;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 40;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		tags  = FIELD_TAGS .value(bytes);
		label = FIELD_LABEL.value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_TAGS.bytes(tags))
				.put(FIELD_LABEL.bytes(label));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
