package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class ByteField extends Field<ByteBuffer> {

	public ByteField() {
	}

	public ByteField(int length) {
		super(length);
	}
	
	@Override
	public int defaultLength() {
		return 2;
	}

	@Override
	public ByteBuffer value(ByteBuffer bytes) {
		byte[] data = new byte[length];
		bytes.get(data);
		
		return ByteBuffer.wrap(data);
	}

	@Override
	public ByteBuffer bytesInternal(ByteBuffer value) {
		return value;
	}
	
}
