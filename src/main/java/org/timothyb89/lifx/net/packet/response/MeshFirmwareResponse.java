package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.LittleField;
import org.timothyb89.lifx.net.field.StringField;
import org.timothyb89.lifx.net.field.UInt32Field;
import org.timothyb89.lifx.net.field.UInt8Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@ToString(callSuper = true)
public class MeshFirmwareResponse extends Packet {

	public static final int TYPE = 0x0F;
	
	public static final Field<Integer> FIELD_BUILD_SECOND   = new UInt8Field();
	public static final Field<Integer> FIELD_BUILD_MINUTE   = new UInt8Field();
	public static final Field<Integer> FIELD_BUILD_HOUR     = new UInt8Field();
	public static final Field<Integer> FIELD_BUILD_DAY      = new UInt8Field();
	public static final Field<String>  FIELD_BUILD_MONTH    = new LittleField(new StringField(3));
	public static final Field<Integer> FIELD_BUILD_YEAR     = new UInt8Field();
	public static final Field<Integer> FIELD_INSTALL_SECOND = new UInt8Field();
	public static final Field<Integer> FIELD_INSTALL_MINUTE = new UInt8Field();
	public static final Field<Integer> FIELD_INSTALL_HOUR   = new UInt8Field();
	public static final Field<Integer> FIELD_INSTALL_DAY    = new UInt8Field();
	public static final Field<String>  FIELD_INSTALL_MONTH  = new LittleField(new StringField(3));
	public static final Field<Integer> FIELD_INSTALL_YEAR   = new UInt8Field();
	public static final Field<Long>    FIELD_VERSION        = new LittleField(new UInt32Field());
	
	@Getter private int buildSecond;
	@Getter private int buildMinute;
	@Getter private int buildHour;
	@Getter private int buildDay;
	@Getter private String buildMonth;
	@Getter private int buildYear;
	
	@Getter private int installSecond;
	@Getter private int installMinute;
	@Getter private int installHour;
	@Getter private int installDay;
	@Getter private String installMonth;
	@Getter private int installYear;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 20;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		buildSecond = FIELD_BUILD_SECOND.value(bytes);
		buildMinute = FIELD_BUILD_MINUTE.value(bytes);
		buildHour   = FIELD_BUILD_HOUR  .value(bytes);
		buildDay    = FIELD_BUILD_DAY   .value(bytes);
		buildMonth  = FIELD_BUILD_MONTH .value(bytes);
		buildYear   = FIELD_BUILD_YEAR  .value(bytes);
		
		installSecond = FIELD_INSTALL_SECOND.value(bytes);
		installMinute = FIELD_INSTALL_MINUTE.value(bytes);
		installHour   = FIELD_INSTALL_HOUR  .value(bytes);
		installDay    = FIELD_INSTALL_DAY   .value(bytes);
		installMonth  = FIELD_INSTALL_MONTH .value(bytes);
		installYear   = FIELD_INSTALL_YEAR  .value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_BUILD_SECOND.bytes(buildSecond))
				.put(FIELD_BUILD_MINUTE.bytes(buildMinute))
				.put(FIELD_BUILD_HOUR.bytes(buildHour))
				.put(FIELD_BUILD_DAY.bytes(buildDay))
				.put(FIELD_BUILD_MONTH.bytes(buildMonth))
				.put(FIELD_BUILD_YEAR.bytes(buildYear))
				.put(FIELD_INSTALL_SECOND.bytes(installSecond))
				.put(FIELD_INSTALL_MINUTE.bytes(installMinute))
				.put(FIELD_INSTALL_HOUR.bytes(installHour))
				.put(FIELD_INSTALL_DAY.bytes(installDay))
				.put(FIELD_INSTALL_MONTH.bytes(installMonth))
				.put(FIELD_INSTALL_YEAR.bytes(installYear));
				
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
