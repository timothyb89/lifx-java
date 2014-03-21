lifx-java
=========

A Java library for interacting with LIFX WiFi LED bulbs, targetting both desktop Java 7 and recent Android releases (4.4+, requires full JDK 7 support). Heavily inspired by and partially based on [lifxjs](https://github.com/magicmonkey/lifxjs).

This library is not yet finished! Currently it only supports gateway and bulb discovery, but support for most other features should be added soon.

Requirements
------------

* JDK 7 or greater (NIO, etc)
* Maven
* [EventBus](https://github.com/timothyb89/EventBus)

Building
--------
```
mvn install
```

Note that [EventBus](https://github.com/timothyb89/EventBus) must be installed first (using the same procedure).

Quickstart
----------
The rough procedure for getting access to a Gateway or a Bulb object (primarily what is interacted with) is:

1. Use a `BroadcastListener` to search for gateway bulbs
2. Wait for a `GatewayDiscoveredEvent`, connect to the gateway
3. Send packets directly to the gateway, or wait for a `GatewayBulbDiscoveredEvent`

In code:

```java
public class MyClient {

	public TestClient() throws IOException {
		BroadcastListener listener = new BroadcastListener();
		listener.bus().register(this);
		listener.startListen();
	}
	
	@EventHandler
	public void gatewayFound(GatewayDiscoveredEvent ev) {
		Gateway g = ev.getGateway();
		g.bus().register(this);
		
		try {
			g.connect(); // automatically discovers bulbs
		} catch (IOException ex) { }
	}
	
	@EventHandler
	public void bulbDiscovered(GatewayBulbDiscoveredEvent event) throws IOException {
		// send some packets
		event.getBulb().send(new SetPowerStateRequest(PowerState.OFF));
	}
	
}
```

Wrapper methods for some common commands should be added soon (e.g. `Bulb.turnOff()`, etc) as well as more packet types. For now all defined packet types can be found in the package [`org.timothyb89.lifx.net.packet`](https://github.com/timothyb89/lifx-java/tree/master/src/main/java/org/timothyb89/lifx/net/packet).

Android support
---------------

The library will attempt to discover and create [MulticastLocks](http://developer.android.com/reference/android/net/wifi/WifiManager.MulticastLock.html), which are required to receive multicast packets on Android devices. However, no binary dependency is created on any Android APIs, so the library can still be used on desktop JREs without any trouble.

This feature was unabashedly stolen (with some adaptions) from [AndroidLIFX](https://github.com/akrs/AndroidLIFX).
