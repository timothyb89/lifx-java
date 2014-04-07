package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.FloatField;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt32Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class WifiInfoResponse extends Packet {

	public static final int TYPE = 0x11;
	
	public static final Field<Float>   FIELD_SIGNAL = new FloatField().little();
	public static final Field<Long>    FIELD_RX     = new UInt32Field().little();
	public static final Field<Long>    FIELD_TX     = new UInt32Field().little();
	public static final Field<Integer> FIELD_TEMP   = new UInt16Field();
	
	@Getter private float signal;
	@Getter private long rx;
	@Getter private long tx;
	@Getter private int mcuTemperature;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 14;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		signal         = FIELD_SIGNAL.value(bytes);
		rx             = FIELD_RX    .value(bytes);
		tx             = FIELD_TX    .value(bytes);
		mcuTemperature = FIELD_TEMP  .value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_SIGNAL.bytes(signal))
				.put(FIELD_RX.bytes(rx))
				.put(FIELD_TX.bytes(tx))
				.put(FIELD_TEMP.bytes(mcuTemperature));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
