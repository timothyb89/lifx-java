package org.timothyb89.lifx.bulb;

/**
 *
 * @author tim
 */
public enum PowerState {
	
	ON(0xFFFF),
	OFF(0x0000);
	
	private int value;
	
	private PowerState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static PowerState fromValue(int value) {
		for (PowerState p : values()) {
			if (p.getValue() == value) {
				return p;
			}
		}
		
		return null;
	}
	
}
