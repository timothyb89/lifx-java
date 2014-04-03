package org.timothyb89.lifx.bulb;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.timothyb89.eventbus.Event;

/**
 * A catchall event for any bulb status change.
 * @author tim
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BulbStatusUpdatedEvent extends BulbPowerStateUpdatedEvent {

	private final LIFXColor color;
	private final int dim;
	private final String label;
	private final long tags;
	
	public BulbStatusUpdatedEvent(
			Bulb bulb, PowerState powerState,
			LIFXColor color, int dim, String label, long tags) {
		super(bulb, powerState);
		this.color = color;
		this.dim = dim;
		this.label = label;
		this.tags = tags;
	}
	
}
