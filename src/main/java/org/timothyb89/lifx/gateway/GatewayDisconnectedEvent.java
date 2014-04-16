package org.timothyb89.lifx.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;

/**
 * An event dispatched when the TCP connection to a gateway has been terminated,
 * either due to a {@code disconnect()} call, connection termination by a bulb
 * due to inactivity, or a network error.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayDisconnectedEvent extends Event {
	
	private final Gateway gateway;
	
}
