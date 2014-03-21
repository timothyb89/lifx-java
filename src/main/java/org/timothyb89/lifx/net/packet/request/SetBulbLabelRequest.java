package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.StringField;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class SetBulbLabelRequest extends Packet {

	public static final int TYPE = 0x18;
	
	public static final Field<String> FIELD_LABEL = new StringField(32);
	
	@Getter @Setter private String label;

	public SetBulbLabelRequest(String label) {
		this.label = label;
	}
	
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
		return new int[] {}; // ?
	}
	
}
