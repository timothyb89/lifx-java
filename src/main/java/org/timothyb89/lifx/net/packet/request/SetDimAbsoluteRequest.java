package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt32Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author timothyb89
 */
public class SetDimAbsoluteRequest extends Packet {

	public static final int TYPE = 0x68;
	
	public static final Field<Integer> FIELD_DIM      = new UInt16Field().little();
	public static final Field<Long>    FIELD_DURATION = new UInt32Field().little();
	
	@Getter @Setter private int dim;
	@Getter @Setter private long duration;

	public SetDimAbsoluteRequest() {
		
	}

	public SetDimAbsoluteRequest(int dim, long duration) {
		this.dim = dim;
		this.duration = duration;
	}
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 6;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		dim      = FIELD_DIM.value(bytes);
		duration = FIELD_DURATION.value(bytes);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_DIM     .bytes(dim))
				.put(FIELD_DURATION.bytes(duration));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
