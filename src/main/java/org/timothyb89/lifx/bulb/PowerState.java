package org.timothyb89.lifx.bulb;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents bulb power states (on or off).
 * @author tim
 */
@Slf4j
public enum PowerState {
	
	ON(0xFFFF),
	OFF(0x0000);
	
	private final int value;
	
	private PowerState(int value) {
		this.value = value;
	}

	/**
	 * Gets the integer value of this power state.
	 * @return the integer value
	 */
	public int getValue() {
		return value;
	}
	
	public static PowerState fromValue(int value) {
		for (PowerState p : values()) {
			if (p.getValue() == value) {
				return p;
			}
		}
		
		log.warn("Unknown power state ID: {}", value);
		
		return null;
	}
	
}
