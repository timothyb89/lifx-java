lifx-java
=========

An unofficial Java library for interacting with LIFX WiFi LED bulbs, targetting both desktop
Java 7 and recent Android releases (4.4+, requires full JDK 7 support). This library would not have been possible without the work of the
[lifxjs project](https://github.com/magicmonkey/lifxjs).

Current State
-------------

This library isn't yet complete and may have unintended bugs. Currently API support exists for bulb discovery, power management (on / off), color changing, and event notifications. Most of the interesting [packet types](https://github.com/magicmonkey/lifxjs/blob/master/Protocol.md) have at least been defined and can be sent and received.

Some features still needing to be implemented:
 * Tags (partially implemented)
 * UDP events: many packets are sent out via UDP as well as TCP
 * Network stability should be improved (automatic reconnect, etc)
 * Remaining packet definitions

### Why use this?

An [official Android API](https://github.com/LIFX/lifx-sdk-android) was released shortly after this library. There are some advantagse to using this library over the official SDK:
 * A (subjectively) nicer API
 * Desktop Java support
 * Event support: get notified when a bulb turns off, changes color, etc
 * Extra utilities: convert RGB -> HSBK, etc

On the other hand, the official SDK:
 * Presumably is more stable / better tested
 * Has full tag support
 * Is official

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
  color, brightness, tags, etc

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
