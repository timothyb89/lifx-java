package org.timothyb89.lifx.gateway;

import java.util.concurrent.Future;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayPacketSentEvent extends Event {
	
	private final Packet sentPacket;
	private final Future<PacketResponse> response;
	
}
