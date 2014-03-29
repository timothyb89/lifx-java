package org.timothyb89.lifx.bulb;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;

/**
 *
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

	public LIFXColor() {
	}

	public LIFXColor(LightStatusResponse packet) {
		hue = packet.getHue();
		saturation = packet.getSaturation();
		value = packet.getBrightness();
		kelvin = packet.getKelvin();
	}
	
	public LIFXColor(int hue, int saturation, int value, int kelvin) {
		this.hue = hue;
		this.saturation = saturation;
		this.value = value;
		this.kelvin = kelvin;
	}
	
	public LIFXColor hue(int hue) {
		return new LIFXColor(hue, this.saturation, this.value, this.kelvin);
	}
	
	public LIFXColor saturation(int saturation) {
		return new LIFXColor(this.hue, saturation, this.value, this.kelvin);
	}
	
	public LIFXColor value(int value) {
		return new LIFXColor(this.hue, this.saturation, value, this.kelvin);
	}
	
	public LIFXColor kelvin(int kelvin) {
		return new LIFXColor(this.hue, this.saturation, this.value, kelvin);
	}
	
	//public void setRGB(int red )
	
	/**
	 * Creates a new LIFXColor in the HSV space from the given red/green/blue
	 * values. Note that values should fall in the range of 0...255 (inclusive).
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
	
	public static void main(String[] args) {
		System.out.println("red:   " + fromRGB(255, 0, 0));
		System.out.println("green: " + fromRGB(0, 255, 0));
		System.out.println("blue:  " + fromRGB(0, 0, 255));
	}
	
}
