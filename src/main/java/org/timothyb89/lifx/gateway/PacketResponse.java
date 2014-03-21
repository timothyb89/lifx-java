package org.timothyb89.lifx.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;

/**
 * Represents a packet response. By default responses consist of packet types
 * defined in {@link Packet#expectedResponses()}, but additional expectations
 * may be defined at sending time with {@link #expect(int)} and
 * {@link #expect(int, MACAddress)}.
 * @author tim
 */
@Data
public class PacketResponse {
	
	private final Packet packet;
	
	private List<Integer> expecting;
	private List<SourcedExpectation> expectingSourced;
	private List<Packet> responses;

	public PacketResponse(Packet packet) {
		this.packet = packet;
		
		expecting = new LinkedList<>();
		for (int type : packet.expectedResponses()) {
			expecting.add(type);
		}
		
		expectingSourced = new LinkedList<>();
		
		responses = new LinkedList<>();
	}
	
	private SourcedExpectation getExpectation(int type, MACAddress address) {
		for (SourcedExpectation e : expectingSourced) {
			if (e.type == type && e.address.equals(address)) {
				return e;
			}
		}
		
		return null;
	}
	
	public boolean isExpecting(int packetType) {
		return expecting.contains(packetType);
	}
	
	public boolean isExpecting(int packetType, MACAddress address) {
		return isExpecting(packetType)
				|| getExpectation(packetType, address) != null;
	}
	
	public boolean isFulfilled() {
		return expecting.isEmpty() && expectingSourced.isEmpty();
	}
	
	public void addResponse(Packet response) {
		int type = response.getPacketType();
		
		if (expecting.contains(type)) {
			expecting.remove((Integer) response.getPacketType());
			responses.add(response);
			return;
		}
		
		SourcedExpectation ex = getExpectation(type, response.getBulbAddress());
		if (ex != null) {
			expectingSourced.remove(ex);
			responses.add(response);
			return;
		}
		
		throw new IllegalArgumentException(String.format(
				"Unexpected response: 0x%02X", response.getPacketType()));
	}
	
	/**
	 * Expects a response with a packet type of {@code type}.
	 * @param type the type to match
	 * @return this PacketResponse
	 */
	public PacketResponse expect(int type) {
		expecting.add(type);
		return this;
	}
	
	/**
	 * Expects a response with a packet type of {@code type} and a matching
	 * {@code bulbAddress}.
	 * @param type the type to match
	 * @param address the bulb MAC address to match
	 * @return this PacketResponse
	 */
	public PacketResponse expect(int type, MACAddress address) {
		expectingSourced.add(new SourcedExpectation(address, type));
		return this;
	}
	
	/**
	 * Gets the first packet of the given type from the list of responses. If no
	 * matching packet is found, {@code null} is returned. Note that order is
	 * not guaranteed; response ordering is "best effort".
	 * @param <T> the packet type
	 * @param type the type of the packet to 
	 * @return a matching packet instance, if any
	 */
	public <T extends Packet> T get(Class<T> type) {
		for (Packet response : responses) {
			if (type.isInstance(response)) {
				return (T) response;
			}
		}
		
		return null;
	}
	
	private class SourcedExpectation {
		
		private final MACAddress address;
		private final int type;

		private SourcedExpectation(MACAddress address, int type) {
			this.address = address;
			this.type = type;
		}
		
	}
	
}
