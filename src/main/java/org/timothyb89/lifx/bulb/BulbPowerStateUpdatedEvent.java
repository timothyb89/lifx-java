package org.timothyb89.lifx.bulb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;

/**
 *
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BulbPowerStateUpdatedEvent extends Event {
	
	private final Bulb bulb;
	private final PowerState powerState;
	
}
