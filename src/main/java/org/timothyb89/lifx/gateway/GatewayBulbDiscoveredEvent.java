package org.timothyb89.lifx.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.bulb.Bulb;

/**
 * An event dispatched when a new bulb has been discovered by a gateway. Bulbs
 * are most often discovered immediately following a successful connection to
 * a gateway, but {@link Gateway#refreshBulbs()} may be called to attempt bulb
 * discovery again.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayBulbDiscoveredEvent extends Event {
	
	private final Gateway gateway;
	private final Bulb bulb;
	
}
