lifx-java
=========

A Java library for interacting with LIFX WiFi LED bulbs, targetting both desktop Java 7 and recent Android releases (4.4+, requires full JDK 7 support). Heavily inspired by and partially based on [lifxjs](https://github.com/magicmonkey/lifxjs).

This library is not yet finished! Currently it only supports UDP gateway discovery, but support for most other features should be added soon.

Requirements
------------

* JDK 7 or greater (NIO, etc)
* Maven

Building
--------
```
mvn install
```

Android support
---------------

The library will attempt to discover and create [MulticastLocks](http://developer.android.com/reference/android/net/wifi/WifiManager.MulticastLock.html), which are required to receive multicast packets on Android devices. However, no binary dependency is created on any Android APIs, so the library can still be used on desktop JREs without any trouble.

This feature was unabashedly stolen from [AndroidLIFX](https://github.com/akrs/AndroidLIFX).
