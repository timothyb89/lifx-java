package org.timothyb89.lifx.net.packet.request;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.field.LittleField;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class PANGatewayRequest extends Packet {

	public static final int PROTOCOL_DEFAULT = 21504; // ??
	
	public PANGatewayRequest() {
		protocol = PROTOCOL_DEFAULT;
	}
	
	@Override
	public int packetLength() {
		return 0;
	}

	@Override
	public int packetType() {
		return 0x02;
	}

	@Override
	protected void parsePacket(ByteBuffer bytes) {
		// empty
	}

	@Override
	protected ByteBuffer packetBytes() {
		return ByteBuffer.allocate(0);
	}
	
	public static void printBuffer(ByteBuffer a) {
		System.out.println(a);
		
		a.rewind();
		
		while (a.hasRemaining()) {
			System.out.print(Integer.toHexString(a.get()) + ' ');
		}
		
		a.rewind();
		System.out.println("\n");
	}
	
	public static void main(String[] args) {
		UInt16Field f = new UInt16Field();
		ByteBuffer fb = f.bytes(36);
		printBuffer(fb);
		
		LittleField lf = new LittleField(f);
		printBuffer(lf.bytes(36));
		
		ByteBuffer lfb = ByteBuffer.allocate(2).put(lf.bytes(36));
		printBuffer(lfb);
		
		PANGatewayRequest r = new PANGatewayRequest();
		printBuffer(r.preambleBytes());
		
		System.out.println(r.packetBytes());
		System.out.println(r.bytes());
	}
	
}
