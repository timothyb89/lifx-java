package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.timothyb89.lifx.bulb.PowerState;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class SetPowerStateRequest extends Packet {

	public static final int TYPE = 0x15;
	
	public static final Field<Integer> FIELD_STATE = new UInt16Field();
	
	@Getter @Setter private PowerState state;

	public SetPowerStateRequest() {
		state = PowerState.OFF;
		
		protocol = 0x1400;
	}
	
	public SetPowerStateRequest(PowerState state) {
		this.state = state;
		
		protocol = 0x1400;
	}
	
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
		state = PowerState.fromValue(FIELD_STATE.value(bytes));
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(2)
				.put(FIELD_STATE.bytes(state.getValue()));
	}

	@Override
	public int[] expectedResponses() {
		return new int[] {};
	}
	
}
