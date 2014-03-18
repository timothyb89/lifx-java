package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class UInt32Field extends Field<Long> {
	
	@Override
	public int defaultLength() {
		return 4;
	}

	@Override
	public Long value(ByteBuffer bytes) {
		return (long) bytes.getInt() & 0xFFFFFFFFL;
	}

	@Override
	public ByteBuffer bytesInternal(Long value) {
		return ByteBuffer
				.allocate(4)
				.putInt((int) (value & 0xFFFFFFFFL));
	}
	
}
