package org.timothyb89.lifx.net.packet.response;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.bulb.PowerState;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@ToString(callSuper = true)
public class PowerStateResponse extends Packet {

	public static final int TYPE = 0x16;
	
	public static final Field<Integer> FIELD_STATE = new UInt16Field();
	
	@Getter private PowerState state;
	
	@Override
	public int packetType() {
		return TYPE;
	}

	@Override
	protected int packetLength() {
		return 2;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		int stateValue = FIELD_STATE.value(bytes);
		state = PowerState.fromValue(stateValue);
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(packetLength())
				.put(FIELD_STATE.bytes(state.getValue()));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
