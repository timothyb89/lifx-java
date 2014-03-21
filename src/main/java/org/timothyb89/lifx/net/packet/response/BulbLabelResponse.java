package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.StringField;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class BulbLabelResponse extends Packet {

	public static final int TYPE = 0x19;
	
	public static final Field<String> FIELD_LABEL = new StringField(32);
	
	@Getter private String label;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 32;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		label = FIELD_LABEL.value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return FIELD_LABEL.bytes(label);
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
