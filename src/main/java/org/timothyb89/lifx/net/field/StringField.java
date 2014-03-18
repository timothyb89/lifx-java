package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author tim
 */
public class StringField extends Field<String> {

	public static final Charset CHARSET = StandardCharsets.US_ASCII;

	public StringField() {
	}
	
	public StringField(int length) {
		super(length);
	}
	
	@Override
	public int defaultLength() {
		return 3;
	}

	@Override
	public String value(ByteBuffer bytes) {
		byte[] buf = new byte[length];
		bytes.get(buf);
		
		ByteBuffer field = ByteBuffer.wrap(buf);
		
		return CHARSET.decode(field).toString();
	}

	@Override
	public ByteBuffer bytesInternal(String value) {
		return CHARSET.encode(value);
	}
	
}
