package org.timothyb89.lifx.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.net.packet.Packet;

/**
 * An event dispatched when a packet has been received via a TCP gateway
 * connection.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayPacketReceivedEvent extends Event {
	
	private final Gateway gateway;
	private final Packet packet;
	
}
