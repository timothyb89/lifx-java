package org.timothyb89.lifx.net;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.bulb.Gateway;

/**
 *
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayFoundEvent extends Event {
	
	private final Gateway gateway;
	
}
