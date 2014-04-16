package org.timothyb89.lifx.gateway;

import java.util.concurrent.Future;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.net.packet.Packet;

/**
 * An event dispatched when a packet has been sent from this client to a
 * gateway. A response future reference is also provided to monitor responses.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayPacketSentEvent extends Event {
	
	private final Gateway gateway;
	private final Packet sentPacket;
	private final PacketResponseFuture response;
	
}
