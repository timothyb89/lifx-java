package org.timothyb89.lifx.bulb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;

/**
 * A catchall event for any bulb status change.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BulbStatusUpdatedEvent extends Event {
	
	private final Bulb bulb;
	
}
