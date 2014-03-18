package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;

/**
 *
 * @author tim
 */
public class MACAddressField extends Field<MACAddress> {

	public MACAddressField() {
		super(6);
	}

	@Override
	public int defaultLength() {
		return 6;
	}

	@Override
	public MACAddress value(ByteBuffer bytes) {
		byte[] data = new byte[length];
		bytes.get(data);
		
		return new MACAddress(ByteBuffer.wrap(data));
	}

	@Override
	protected ByteBuffer bytesInternal(MACAddress value) {
		return value.getBytes().duplicate();
	}
	
}
