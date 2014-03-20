package org.timothyb89.lifx.net.packet;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.PacketResponseFuture;
import org.timothyb89.lifx.net.field.ByteField;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.LittleField;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.field.MACAddressField;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt64Field;

/**
 * Represents an abstract packet, providing conversion functionality to and from
 * {@link ByteBuffer}s for common packet (preamble) fields. Subtypes of this
 * class can provide conversion functionality for specialized fields.
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
	@Getter @Setter protected MACAddress site;
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
	
	/**
	 * Sets default preamble values. If needed, subclasses may override these
	 * values by specifically overriding this method, or by setting individual
	 * values within the constructor, as this method is called automatically
	 * during initialization.
	 */
	protected void preambleDefaults() {
		size = 0; // not used when sending
		protocol = 13312; // ?
		bulbAddress = new MACAddress();
		site = new MACAddress();
		timestamp = 0;
		packetType = packetType();
	}
	
	/**
	 * Returns the packet type. Note that this value is technically distinct
	 * from {@link #getPacketType()} in that it returns the packet type the
	 * current {@code Packet} subtype is designed to parse, while
	 * {@code getPacketType()} returns the actual {@code packetType} field of
	 * a parsed packet. However, these values should always match.
	 * @return the packet type intended to be handled by this Packet subtype
	 */
	public abstract int packetType();
	
	/**
	 * Returns the length of the payload specific to this packet subtype. The
	 * length of the preamble is specifically excluded.
	 * @return the length of this specialized packet payload
	 */
	protected abstract int packetLength();
	
	/**
	 * Parses the given {@link ByteBuffer} into class fields. Subtypes may
	 * implement {@link #parsePacket(ByteBuffer)} to parse additional fields;
	 * the preamble by default is always parsed.
	 * @param bytes the buffer to extract data from
	 */
	public void parse(ByteBuffer bytes) {
		//ByteBufferTest.printBuffer(bytes);
		bytes.rewind();
		parsePreamble(bytes);
		parsePacket(bytes);
	}
	
	/**
	 * Extracts data from the given {@link ByteBuffer} into fields specific to
	 * this packet subtype. The preamble will already have been parsed; as such,
	 * the buffer will be positioned at the end of the preamble. If needed,
	 * {@link #preambleLength()} may be used to restore the position of the
	 * buffer.
	 * @param bytes the raw bytes to parse
	 */
	protected abstract void parsePacket(ByteBuffer bytes);
	
	/**
	 * Returns a {@link ByteBuffer} containing the full payload for this packet,
	 * including the populated preamble and any specialized packet payload. The
	 * returned buffer will be at position zero.
	 * @return the full packet payload
	 */
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
	
	/**
	 * Returns a {@link ByteBuffer} containing the payload for this packet. Its
	 * length must match the value of {@link #packetLength()}. This specifically
	 * excludes preamble fields and should contain only data specific to the
	 * packet subtype.
	 * <p>Note that returned ByteBuffers will have {@link ByteBuffer#rewind()}
	 * called automatically before they are appended to the final packet
	 * buffer.</p>
	 * @return the packet payload
	 */
	protected abstract ByteBuffer packetBytes();
	
	/**
	 * Gets the total length of this packet, in bytes.
	 * @return the total length of this packet
	 */
	public int length() {
		return preambleLength() + packetLength();
	}
	
	/**
	 * Returns a list of expected response packet types. An empty array means
	 * no responses are expected (suitable for response packet definitions),
	 * while one or more values will cause {@link PacketResponseFuture}
	 * instances returned by {@link Gateway} to wait until all packets of all
	 * the listed types have been received before returning a value.
	 * 
	 * <p>Note that optional response packets, or responses sent over UDP,
	 * should not be listed here. Any instance where an optional packet is not
	 * received will prevent other futures from being fulfilled.</p>
	 * 
	 * @return a list of expected responses
	 */
	public abstract int[] expectedResponses();
	
}
