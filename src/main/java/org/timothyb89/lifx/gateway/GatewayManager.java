package org.timothyb89.lifx.gateway;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Manages the global list of known gateways.
 * @author tim
 */
public class GatewayManager {
	
	private static GatewayManager instance;
	
	@Getter private List<Gateway> gateways;
	
	private GatewayManager() {
		gateways = new ArrayList<>();
	}

	public static synchronized GatewayManager getInstance() {
		if (instance == null) {
			instance = new GatewayManager();
		}
		
		return instance;
	}
	
	public void registerGateway(Gateway g) {
		gateways.add(g);
	}
	
	public boolean hasGateway(InetSocketAddress address, int port) {
		for (Gateway g : gateways) {
			if (g.getIpAddress().equals(address) && g.getPort() == port) {
				return true;
			}
		}
		
		return false;
	}
	
	public Gateway getGateway(InetSocketAddress address, int port) {
		for (Gateway g : gateways) {
			if (g.getIpAddress().equals(address) && g.getPort() == port) {
				return g;
			}
		}
		
		return null;
	}
	
	public Gateway getGateway(InetSocketAddress address) {
		for (Gateway g : gateways) {
			if (g.getIpAddress().equals(address)) {
				return g;
			}
		}
		
		return null;
	}
	
	/**
	 * Purges the list of gateways and bulbs. Note that any references still
	 * held may or may not remain valid indefinitely; bulbs may "disappear" to
	 * the client if the host system changes networks, if a bulb is turned off,
	 * or for any number of other reasons. 
	 */
	public void purge() {
		gateways = new ArrayList<>();
	}
	
}
