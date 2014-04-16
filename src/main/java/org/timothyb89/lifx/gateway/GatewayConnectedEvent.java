package org.timothyb89.lifx.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;

/**
 * An event dispatched when a TCP connection to a gateway has been established.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayConnectedEvent extends Event {
	
	private final Gateway gateway;
	
}
