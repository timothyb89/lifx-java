package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.timothyb89.lifx.net.field.ByteField;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt32Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class SetLightColorRequest extends Packet {

	public static final int TYPE = 0x66;
	
	public static final Field<ByteBuffer> FIELD_STREAM     = new ByteField(1);
	public static final Field<Integer>    FIELD_HUE        = new UInt16Field().little();
	public static final Field<Integer>    FIELD_SATURATION = new UInt16Field().little();
	public static final Field<Integer>    FIELD_BRIGHTNESS = new UInt16Field().little();
	public static final Field<Integer>    FIELD_KELVIN     = new UInt16Field().little();
	public static final Field<Long>       FIELD_FADE_TIME  = new UInt32Field().little();
	
	@Getter private ByteBuffer stream;
	
	@Getter @Setter private int hue;
	@Getter @Setter private int saturation;
	@Getter @Setter private int brightness;
	@Getter @Setter private int kelvin;
	@Getter @Setter private long fadeTime;

	public SetLightColorRequest() {
		stream = ByteBuffer.allocate(1);
	}
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 13;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		stream     = FIELD_STREAM    .value(bytes);
		hue        = FIELD_HUE       .value(bytes);
		saturation = FIELD_SATURATION.value(bytes);
		brightness = FIELD_BRIGHTNESS.value(bytes);
		kelvin     = FIELD_KELVIN    .value(bytes);
		fadeTime   = FIELD_FADE_TIME .value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_STREAM    .bytes(stream))
				.put(FIELD_HUE       .bytes(hue))
				.put(FIELD_SATURATION.bytes(saturation))
				.put(FIELD_BRIGHTNESS.bytes(brightness))
				.put(FIELD_KELVIN    .bytes(kelvin))
				.put(FIELD_FADE_TIME .bytes(fadeTime));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
