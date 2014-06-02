package org.timothyb89.lifx.net.packet;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.timothyb89.lifx.gateway.Gateway;
import org.timothyb89.lifx.gateway.PacketResponseFuture;
import org.timothyb89.lifx.net.field.ByteField;
import org.timothyb89.lifx.net.field.Field;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.field.MACAddressField;
import org.timothyb89.lifx.net.field.UInt16Field;
import org.timothyb89.lifx.net.field.UInt64Field;

/**
 * Represents an abstract packet, providing conversion functionality to and from
 * {@link ByteBuffer}s for common packet (preamble) fields. Subtypes of this
 * class can provide conversion functionality for specialized fields.
 * 
 * <p>Defining new packet types essentially involves extending this class,
 * defining the fields and implementing {@link #packetType()},
 * {@link #packetLength()}, and {@link #packetBytes()}. By convention, packet
 * type should be stored in a {@code public static final int PACKET_TYPE} field
 * in each subtype, followed by a listing of fields contained in the packet.
 * Field definitions should remain accessible to outside classes in the event
 * they need to worked with directly elsewhere.</p>
 * @author tim
 */
@ToString(of = { "packetType", "size", "bulbAddress" })
public abstract class Packet {

	public static final Field<Integer>    FIELD_SIZE         = new UInt16Field().little();
	public static final Field<Integer>    FIELD_PROTOCOL     = new UInt16Field().little();
	public static final Field<ByteBuffer> FIELD_RESERVED_1   = new ByteField(4);
	public static final Field<MACAddress> FIELD_BULB_ADDRESS = new MACAddressField();
	public static final Field<ByteBuffer> FIELD_RESERVED_2   = new ByteField(2);
	public static final Field<MACAddress> FIELD_SITE         = new MACAddressField();
	public static final Field<ByteBuffer> FIELD_RESERVED_3   = new ByteField(2);
	public static final Field<Long>       FIELD_TIMESTAMP    = new UInt64Field();
	public static final Field<Integer>    FIELD_PACKET_TYPE  = new UInt16Field().little();
	public static final Field<ByteBuffer> FIELD_RESERVED_4   = new ByteField(2);
	
	/**
	 * An ordered array of all fields contained in the common packet preamble.
	 */
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
	@Getter @Setter protected MACAddress bulbAddress;
	@Getter protected ByteBuffer reserved2;
	@Getter @Setter protected MACAddress site;
	@Getter protected ByteBuffer reserved3;
	@Getter protected long timestamp;
	@Getter protected int packetType;
	@Getter protected ByteBuffer reserved4;
	
	/**
	 * Creates an empty packet, setting some default values via
	 * {@link #preambleDefaults()}.
	 */
	public Packet() {
		preambleDefaults();
	}
	
	/**
	 * Parses, in order, the defined preamble fields, storing collected values.
	 * The buffer's position will be left at the end of the parsed fields and
	 * should be equal to the value returned by {@link #preambleLength()}.
	 * @param bytes the buffer to read from.
	 */
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
	
	/**
	 * Calculates the length of the packet header, defined as the sum of the
	 * lengths of all defined fields (see {@link #PREAMBLE_FIELDS}).
	 * @return the sum of the length of preamble fields
	 */
	protected int preambleLength() {
		int sum = 0;
		
		for (Field f : PREAMBLE_FIELDS) {
			sum += f.getLength();
		}
		
		return sum;
	}
	
	/**
	 * Returns a new {@code ByteBuffer} containing the encoded preamble. Note
	 * that the returned buffer will have its position set at the end of the
	 * buffer and will need to have {@link ByteBuffer#rewind()} called before
	 * use.
	 * 
	 * <p>The length of the buffer is the sum of the lengths of the defined
	 * preamble fields (see {@link #PREAMBLE_FIELDS} for an ordered list), which
	 * may also be accessed via {@link #preambleLength()}.</p>
	 * 
	 * <p>Certain fields are set to default values based on other class methods.
	 * For example, the size and packet type fields will be set to the values
	 * returned from {@link #length()} and {@link #packetType()}, respectively.
	 * Other defaults (such as the protocol, bulb address, site, and timestamp)
	 * may be specified either by directly setting the relevant protected
	 * variables or by overriding {@link #preambleDefaults()}.
	 * @return a new buffer containing the encoded preamble
	 */
	protected ByteBuffer preambleBytes() {
		return ByteBuffer.allocate(preambleLength())
				.put(FIELD_SIZE.bytes(length()))
				.put(FIELD_PROTOCOL.bytes(protocol))
				.put(ByteBuffer.allocate(FIELD_RESERVED_1.getLength()))   // empty
				.put(FIELD_BULB_ADDRESS.bytes(bulbAddress))
				.put(ByteBuffer.allocate(FIELD_RESERVED_2.getLength()))  // empty
				.put(FIELD_SITE.bytes(site))
				.put(ByteBuffer.allocate(FIELD_RESERVED_3.getLength()))  // empty
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
	 * from {@code getPacketType()} in that it returns the packet type the
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
	 * Gets the total length of this packet, in bytes. Specifically, this method
	 * is the sum of the preamble ({@link #preambleLength()}) and the payload
	 * length ({@link #packetLength()}); subtypes should override methods for
	 * those values if desired.
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
	 * <p>When providing values here, note that {@link Gateway} will always wait
	 * for all of the defined response types to be fulfilled. If a response
	 * isn't guaranteed it's probably best to leave it out; clients may add
	 * expected response types to {@link PacketResponseFuture} on an individual
	 * basis.</p>
	 * 
	 * @return a list of expected responses
	 */
	public abstract int[] expectedResponses();
	
}
