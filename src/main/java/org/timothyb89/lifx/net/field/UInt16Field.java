package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class UInt16Field extends Field<Integer> {

	@Override
	public int defaultLength() {
		return 2;
	}

	@Override
	public Integer value(ByteBuffer bytes) {
		return bytes.getShort() & 0xFFFF;
	}

	@Override
	public ByteBuffer bytesInternal(Integer value) {
		return ByteBuffer
				.allocate(2)
				.putShort((short) (value & 0xFFFF));
	}
	
}
