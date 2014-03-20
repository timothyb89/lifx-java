package org.timothyb89.lifx.net.field;

import java.nio.ByteBuffer;
import lombok.Getter;

/**
 *
 * @author tim
 * @param <T> the field datatype
 */
public abstract class Field<T> {
	
	@Getter
	protected final int length;

	public Field() {
		length = defaultLength();
	}
	
	public Field(int length) {
		this.length = length;
	}
	
	public abstract int defaultLength();
	
	public abstract T value(ByteBuffer bytes);
	
	public ByteBuffer bytes(T value) {
		ByteBuffer buf = bytesInternal(value);
		buf.rewind();
		return buf;
	}
	
	protected abstract ByteBuffer bytesInternal(T value);
	
	public Field<T> little() {
		return new LittleField<T>(this);
	}
	
}
