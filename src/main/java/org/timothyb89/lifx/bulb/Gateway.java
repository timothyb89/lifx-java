package org.timothyb89.lifx.bulb;

import java.net.InetSocketAddress;
import lombok.Getter;
import lombok.ToString;
import org.timothyb89.lifx.net.field.MACAddress;

/**
 *
 * @author tim
 */
@ToString
public class Gateway {
	
	@Getter private InetSocketAddress ipAddress;
	@Getter private int port;
	@Getter private MACAddress macAddress;

	public Gateway() {
	}

	public Gateway(InetSocketAddress ipAddress, int port, MACAddress macAddress) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.macAddress = macAddress;
	}
	
	
	
}
