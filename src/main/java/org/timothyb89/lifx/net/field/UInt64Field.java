package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 * A pseudo-uint64 field. Bytes will be stored directly in a long value, so
 * unexpected values will likely be shown if exposed to users. Most bit-level
 * operations should still work (addition, multiplication, shifting, etc).
 * @author tim
 */
public class UInt64Field extends Field<Long> {

	@Override
	public int defaultLength() {
		return 8;
	}

	@Override
	public Long value(ByteBuffer bytes) {
		return bytes.getLong();
	}

	@Override
	protected ByteBuffer bytesInternal(Long value) {
		return ByteBuffer
				.allocate(8)
				.putLong(value);
	}
	
}
