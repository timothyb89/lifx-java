package org.timothyb89.lifx.bulb;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;

/**
 * A basic implementation of a color in the HSVK space, compatible with LIFX
 * bulbs. This implementation uses colors ranging from {@link #MIN_VALUE}
 * (0x0000) to {@link #MAX_VALUE} (0xFFFF)
 * @author tim
 */
@ToString
@EqualsAndHashCode
public class LIFXColor {
	
	public static final int MIN_VALUE = 0x0000;
	public static final int MAX_VALUE = 0xFFFF;
	public static final int DEFAULT_KELVIN = 3500;
	
	@Getter private int hue;
	@Getter private int saturation;
	@Getter private int value;
	@Getter private int kelvin;

	/**
	 * Creates a new LIFXColor with unspecified values.
	 */
	public LIFXColor() {
	}
	
	/**
	 * Creates a new LIFXColor using values from the given light status packet.
	 * @param packet the packet to copy values from
	 */
	public LIFXColor(LightStatusResponse packet) {
		hue = packet.getHue();
		saturation = packet.getSaturation();
		value = packet.getBrightness();
		kelvin = packet.getKelvin();
	}
	
	/**
	 * Creates a new LIFXColor using the specified values.
	 * @param hue the hue of the color
	 * @param saturation the saturation of the color
	 * @param value The the `value` parameter for the HSVK color
	 * @param kelvin The kelvin value for the HSVK color. See
	 *     {@link #DEFAULT_KELVIN}.
	 */
	public LIFXColor(int hue, int saturation, int value, int kelvin) {
		this.hue = hue;
		this.saturation = saturation;
		this.value = value;
		this.kelvin = kelvin;
	}
	
	/**
	 * Returns a new LIFXColor with the given hue value, copying other
	 * parameters from this instance.
	 * @param hue the hue value to use
	 * @return a copy of this color with the given hue parameter
	 */
	public LIFXColor hue(int hue) {
		return new LIFXColor(hue, this.saturation, this.value, this.kelvin);
	}
	
	/**
	 * Returns a new LIFXColor with the given saturation value, copying other
	 * parameters from this instance.
	 * @param saturation the saturation value to use
	 * @return a copy of this color with the given saturation parameter
	 */
	public LIFXColor saturation(int saturation) {
		return new LIFXColor(this.hue, saturation, this.value, this.kelvin);
	}
	
	/**
	 * Returns a new LIFXColor with the given {@code value} parameter, copying
	 * other fields from this instance.
	 * @param value the value parameter to use
	 * @return a copy of this color with the given value parameter
	 */
	public LIFXColor value(int value) {
		return new LIFXColor(this.hue, this.saturation, value, this.kelvin);
	}
	
	/**
	 * Returns a new LIFXColor with the given kelvin value, copying other
	 * parameters from this instance.
	 * @param kelvin the kelvin value to use
	 * @return a copy of this color with the given kelvin parameter
	 */
	public LIFXColor kelvin(int kelvin) {
		return new LIFXColor(this.hue, this.saturation, this.value, kelvin);
	}
	
	/**
	 * Creates a new LIFXColor in the HSV space from the given red/green/blue
	 * values. Note that values should fall in the range of 0...255 (inclusive).
	 * The kelvin will be a default of {@link #DEFAULT_KELVIN}.
	 * @param red the red component of the color
	 * @param green the green component of the color
	 * @param blue the blue component of the color
	 * @return a LIFXColor based on the given values
	 */
	public static LIFXColor fromRGB(int red, int green, int blue) {
		double r = ((double) red)   / 255d;
		double g = ((double) green) / 255d;
		double b = ((double) blue)  / 255d;
		
		double min = Math.min(r, Math.min(g, b));
		double max = Math.max(r, Math.max(g, b));
		double range = max - min;
		
		if (max == 0) {
			return new LIFXColor(0, 0, 0, DEFAULT_KELVIN);
		}
		
		double hue;
		double saturation = range / max;
		double value = max;

		// find H'
		if (r == max) {
			hue = (g - b) / range;
		} else if (g == max) {
			hue = 2 + (b - r) / range;
		} else { // b == max
			hue = 4 + (r - g) / range;
		}

		// H' -> H
		hue *= (Math.PI / 3);

		if (hue < 0) {
			hue += 2 * Math.PI;
		}
		
		// convert to the lifx binary format
		// 0x0000 - 0xFFFF, wrapping
		double hueRatio = hue / (2 * Math.PI);
		
		return new LIFXColor(
				(int) (hueRatio * MAX_VALUE),
				(int) (saturation * MAX_VALUE),
				(int) (value * MAX_VALUE),
				DEFAULT_KELVIN);
	}
	
}
