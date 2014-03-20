package org.timothyb89.lifx.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.bulb.Bulb;

/**
 *
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayBulbDiscoveredEvent extends Event {
	
	private final Gateway gateway;
	private final Bulb bulb;
	
}
