package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class UInt8Field extends Field<Integer> {

	public UInt8Field() {
		super(1);
	}
	
	@Override
	public int defaultLength() {
		return 1;
	}

	@Override
	public Integer value(ByteBuffer bytes) {
		return (int) (bytes.get() & 0xFF);
	}

	@Override
	public ByteBuffer bytesInternal(Integer value) {
		return ByteBuffer
				.allocate(1)
				.put((byte) (value & 0xFF));
	}
	
}
