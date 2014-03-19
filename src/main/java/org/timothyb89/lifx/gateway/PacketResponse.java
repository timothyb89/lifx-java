package org.timothyb89.lifx.gateway;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@Data
public class PacketResponse {
	
	private final Packet packet;
	
	private List<Integer> expecting;
	private List<Packet> responses;

	public PacketResponse(Packet packet) {
		this.packet = packet;
		
		expecting = new LinkedList<>();
		for (int type : packet.expectedResponses()) {
			expecting.add(type);
		}
		
		responses = new LinkedList<>();
	}
	
	public boolean isExpecting(int packetType) {
		return expecting.contains(packetType);
	}
	
	public boolean isFulfilled() {
		return expecting.isEmpty();
	}
	
	public void addResponse(Packet response) {
		if (!expecting.contains(response.getPacketType())) {
			throw new IllegalArgumentException(String.format(
					"Unexpected response: 0x%02X", response.getPacketType()));
		}
		
		expecting.remove((Integer) response.getPacketType());
		responses.add(response);
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
	
}
