package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.LittleField;
import org.timothyb89.lifx.net.field.UInt32Field;
import org.timothyb89.lifx.net.field.UInt8Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@ToString(callSuper = true)
public class PANGatewayResponse extends Packet {

	public static final Field<Integer> FIELD_SERVICE = new UInt8Field();
	public static final Field<Long>    FIELD_PORT    = new LittleField(new UInt32Field());

	@Getter private int service;
	@Getter private long port;
	
	public PANGatewayResponse() {
	}
	
	@Override
	protected void parsePacket(ByteBuffer bytes) {
		service = FIELD_SERVICE.value(bytes);
		port    = FIELD_PORT   .value(bytes);
	}

	@Override
	public int packetType() {
		return 0x03;
	}

	@Override
	protected int packetLength() {
		return 5;
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_SERVICE.bytes(service))
				.put(FIELD_PORT.bytes(port));
	}
	
}
