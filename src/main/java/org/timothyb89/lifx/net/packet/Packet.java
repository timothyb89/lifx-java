package org.timothyb89.lifx.net.packet;

import java.nio.ByteBuffer;
import java.util.List;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.net.field.ByteField;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.LittleField;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.field.MACAddressField;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt64Field;

/**
 * 
 * @author tim
 */
@ToString(of = { "packetType", "size", "bulbAddress" })
public abstract class Packet {

	public static final Field<Integer>    FIELD_SIZE         = new LittleField(new UInt16Field());
	public static final Field<Integer>    FIELD_PROTOCOL     = new LittleField(new UInt16Field());
	public static final Field<ByteBuffer> FIELD_RESERVED_1   = new ByteField(4);
	public static final Field<MACAddress> FIELD_BULB_ADDRESS = new MACAddressField();
	public static final Field<ByteBuffer> FIELD_RESERVED_2   = new ByteField(2);
	public static final Field<MACAddress> FIELD_SITE         = new MACAddressField();
	public static final Field<ByteBuffer> FIELD_RESERVED_3   = new ByteField(2);
	public static final Field<Long>       FIELD_TIMESTAMP    = new UInt64Field();
	public static final Field<Integer>    FIELD_PACKET_TYPE  = new LittleField(new UInt16Field());
	public static final Field<ByteBuffer> FIELD_RESERVED_4   = new ByteField(2);
	
	public static final Field[] PREAMBLE_FIELDS = new Field[] {
		FIELD_SIZE,
		FIELD_PROTOCOL,
		FIELD_RESERVED_1,
		FIELD_BULB_ADDRESS,
		FIELD_RESERVED_2,
		FIELD_SITE,
		FIELD_RESERVED_3,
		FIELD_TIMESTAMP,
		FIELD_PACKET_TYPE,
		FIELD_RESERVED_4
	};
	
	@Getter protected int size;
	@Getter protected int protocol;
	@Getter protected ByteBuffer reserved1;
	@Getter protected MACAddress bulbAddress;
	@Getter protected ByteBuffer reserved2;
	@Getter protected MACAddress site;
	@Getter protected ByteBuffer reserved3;
	@Getter protected long timestamp;
	@Getter protected int packetType;
	@Getter protected ByteBuffer reserved4;
	
	public Packet() {
		preambleDefaults();
	}
	
	protected void parsePreamble(ByteBuffer bytes) {
		size        = FIELD_SIZE        .value(bytes);
		protocol    = FIELD_PROTOCOL    .value(bytes);
		reserved1   = FIELD_RESERVED_1  .value(bytes);
		bulbAddress = FIELD_BULB_ADDRESS.value(bytes);
		reserved2   = FIELD_RESERVED_2  .value(bytes);
		site        = FIELD_SITE        .value(bytes);
		reserved3   = FIELD_RESERVED_3  .value(bytes);
		timestamp   = FIELD_TIMESTAMP   .value(bytes);
		packetType  = FIELD_PACKET_TYPE .value(bytes);
		reserved4   = FIELD_RESERVED_4  .value(bytes);
	}
	
	protected int preambleLength() {
		int sum = 0;
		
		for (Field f : PREAMBLE_FIELDS) {
			sum += f.getLength();
		}
		
		return sum;
	}
	
	protected ByteBuffer preambleBytes() {
		return ByteBuffer.allocate(preambleLength())
				.put(FIELD_SIZE.bytes(length()))
				.put(FIELD_PROTOCOL.bytes(protocol))
				.put(ByteBuffer.allocate(FIELD_RESERVED_1.getLength()))   // empty
				.put(FIELD_BULB_ADDRESS.bytes(bulbAddress))
				.put(ByteBuffer.allocate(FIELD_RESERVED_2.getLength()))  // empty
				.put(FIELD_SITE.bytes(site))
				.put(ByteBuffer.allocate(FIELD_RESERVED_3.getLength())) // empty
				.put(FIELD_TIMESTAMP.bytes(timestamp))
				.put(FIELD_PACKET_TYPE.bytes(packetType()))
				.put(ByteBuffer.allocate(FIELD_RESERVED_4.getLength())); // empty
	}
	
	protected void preambleDefaults() {
		size = 0; // not used when sending
		protocol = 13312; // ?
		bulbAddress = new MACAddress();
		site = new MACAddress();
		timestamp = 0;
		packetType = packetType();
	}
	
	public abstract int packetType();
	
	protected abstract int packetLength();
	
	public void parse(ByteBuffer bytes) {
		//ByteBufferTest.printBuffer(bytes);
		bytes.rewind();
		parsePreamble(bytes);
		parsePacket(bytes);
	}
	
	protected abstract void parsePacket(ByteBuffer bytes);
	
	public ByteBuffer bytes() {
		ByteBuffer preamble = preambleBytes();
		preamble.rewind();
		
		ByteBuffer packet = packetBytes();
		packet.rewind();
		
		ByteBuffer ret = ByteBuffer.allocate(length())
				.put(preamble)
				.put(packet);
		ret.rewind();
		
		return ret;
	}
	
	protected abstract ByteBuffer packetBytes();
	
	public int length() {
		return preambleLength() + packetLength();
	}
	
}
