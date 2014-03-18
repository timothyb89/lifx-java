package org.timothyb89.lifx.net.packet;

import java.nio.ByteBuffer;


public class ByteBufferTest {
	
	public static void printBuffer(ByteBuffer a) {
		a.rewind();
		
		while (a.hasRemaining()) {
			System.out.print(Integer.toHexString(a.get()) + ' ');
		}
		
		a.rewind();
		System.out.println("\n");
	}
	
	public static ByteBuffer flip(ByteBuffer buf) {
		ByteBuffer ret = ByteBuffer.allocate(buf.limit());
		
		for (int i = buf.limit() - 1; i >= 0; i--) {
			ret.put(buf.get(i));
		}
		
		return ret;
	}
	
	public static void main(String[] args) {
		ByteBuffer a = ByteBuffer
				.allocate(2)
				.putShort((short) (36 & 0xFFFF));
		printBuffer(a); // 0 24
		
		ByteBuffer other = ByteBuffer
				.allocate(2)
				.put(a);
		printBuffer(other);
		
		printBuffer(flip(other));
		
		//other.rewind();
		
		printBuffer(flip(flip(other)));
	}
	
}
