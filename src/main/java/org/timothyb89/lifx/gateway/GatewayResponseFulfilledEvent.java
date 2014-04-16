package org.timothyb89.lifx.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;

/**
 * An event dispatched when a {@link PacketResponseFuture} has been fulfilled.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayResponseFulfilledEvent extends Event {
	
	private final PacketResponseFuture response;
	
}
