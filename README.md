lifx-java
=========

A Java library for interacting with LIFX WiFi LED bulbs, targetting both desktop
Java 7 and recent Android releases (4.4+, requires full JDK 7 support). Heavily
inspired by and partially based on
[lifxjs](https://github.com/magicmonkey/lifxjs).

Current State
-------------

This library is not yet finished! Currently it supports gateway and bulb
discovery, as well as basic bulb or gateway-level power control.

Preliminary support for color changing has been added (see
[SetLightColorRequest](https://github.com/timothyb89/lifx-java/blob/master/src/main/java/org/timothyb89/lifx/net/packet/request/SetLightColorRequest.java)),
but this hasn't been properly exposed through a nice API yet.

Most events are working for gateways, however event processing for individual
bulbs hasn't yet been implemented. This is the next major feature to be
implemented.

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

Note that [EventBus](https://github.com/timothyb89/EventBus) must be installed
first (using the same procedure).

Quickstart
----------
The rough procedure for getting access to a Gateway or a Bulb object (primarily
what is interacted with) is:

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
		// register for bulb events
		event.getBulb().bus().register(this);
		
		// send some packets
		event.getBulb().turnOff();
		event.getBulb().setColor(LIFXColor.fromRGB(0, 255, 0));
	}

	@EventHandler
	public void bulbUpdated(BulbStatusUpdatedEvent event) {
		System.out.println("bulb updated");
	}
	
}
```

Wrapper methods for some common commands have been added (e.g. `Bulb.turnOff()`,
etc), although many packet types don't have a nice API yet (or haven't been
implemented at all). For now all defined packet types can be found in the
package
[`org.timothyb89.lifx.net.packet`](https://github.com/timothyb89/lifx-java/tree/master/src/main/java/org/timothyb89/lifx/net/packet).

Events
------

Events will be pushed to EventBus instances for various objects. For each class
listed below, you can use:

```java
// call to begin receiving events
instance.bus().register(someObject);

// someObject will be scanned for methods annotated with @EventHandler
@EventHandler
public void someEvent(SomeEventClass clazz) {
	// do something
}
```

### BroadcastListener

* `GatewayBulbDiscoveredEvent`: called when a new `Gateway` has been discovered.

### Gateway

* `GatewayConnectedEvent`: pushed when a connection to the gateway is made
* `GatewayDisconnectedEvent`: pushed when the connection to the gateway is
   closed
* `GatewayBulbDiscoveredEvent`: pushed when a new bulb has been discovered
* `GatewayPacketSendEvent`: called when a packet has been sent to the gateway
* `GatewayPacketReceivedEvent`: called when a packet has been received from a
  gateway
* `GatewayResponseFulfilledEvent`: called when a `PacketResponseFuture`
  expecting some response (or multiple responses) has been fulfilled

### Bulb

* `BulbPowerStateUpdatedEvent`: called when a bulb is turned off or on
* `BulbStatusUpdatedEvent`: called when some other bulb status is updated, e.g.

Android support
---------------

The library will attempt to discover and create
[MulticastLocks](http://developer.android.com/reference/android/net/wifi/WifiManager.MulticastLock.html),
which are required to receive multicast packets on Android devices. However, no
binary dependency is created on any Android APIs, so the library can still be
used on desktop JREs without any trouble.

This feature was unabashedly stolen (with some adaptions) from
[AndroidLIFX](https://github.com/akrs/AndroidLIFX).

Also note that you'll need the
[slf4j android plugin](http://www.slf4j.org/android/) to get log messages.
