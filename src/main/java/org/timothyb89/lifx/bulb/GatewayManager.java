package org.timothyb89.lifx.bulb;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tim
 */
public class GatewayManager {
	
	private static GatewayManager instance;
	
	private final List<Gateway> gateways;
	
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
	
}
