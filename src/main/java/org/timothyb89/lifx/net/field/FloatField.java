package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class FloatField extends Field<Float> {

	@Override
	public int defaultLength() {
		return 4;
	}

	@Override
	public Float value(ByteBuffer bytes) {
		return bytes.getFloat();
	}

	@Override
	protected ByteBuffer bytesInternal(Float value) {
		return ByteBuffer.allocate(4)
				.putFloat(value);
	}
	
}
