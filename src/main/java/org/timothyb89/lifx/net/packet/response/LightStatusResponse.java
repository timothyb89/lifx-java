package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.bulb.PowerState;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.StringField;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt64Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@ToString(callSuper = true)
public class LightStatusResponse extends Packet {
	
	public static final int TYPE = 0x6B;

	public static final Field<Integer> FIELD_HUE        = new UInt16Field().little();
	public static final Field<Integer> FIELD_SATURATION = new UInt16Field().little();
	public static final Field<Integer> FIELD_BRIGHTNESS = new UInt16Field().little();
	public static final Field<Integer> FIELD_KELVIN     = new UInt16Field().little();
	public static final Field<Integer> FIELD_DIM        = new UInt16Field().little();
	public static final Field<Integer> FIELD_POWER      = new UInt16Field();
	public static final Field<String>  FIELD_LABEL      = new StringField(32);
	public static final Field<Long>    FIELD_TAGS       = new UInt64Field();
	
	@Getter private int hue;
	@Getter private int saturation;
	@Getter private int brightness;
	@Getter private int kelvin;
	@Getter private int dim;
	@Getter private PowerState power; // PowerState?
	@Getter private String label;
	@Getter private long tags;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 52;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		hue        = FIELD_HUE       .value(bytes);
		saturation = FIELD_SATURATION.value(bytes);
		brightness = FIELD_BRIGHTNESS.value(bytes);
		kelvin     = FIELD_KELVIN    .value(bytes);
		dim        = FIELD_DIM       .value(bytes);
		power      = PowerState.fromValue(FIELD_POWER.value(bytes));
		label      = FIELD_LABEL     .value(bytes);
		tags       = FIELD_TAGS      .value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_HUE.bytes(hue))
				.put(FIELD_SATURATION.bytes(saturation))
				.put(FIELD_BRIGHTNESS.bytes(brightness))
				.put(FIELD_KELVIN.bytes(kelvin))
				.put(FIELD_DIM.bytes(dim))
				.put(FIELD_POWER.bytes(hue))
				.put(FIELD_LABEL.bytes(label))
				.put(FIELD_TAGS.bytes(tags));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
