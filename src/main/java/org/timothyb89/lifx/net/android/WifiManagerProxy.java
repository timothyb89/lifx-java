package org.timothyb89.lifx.net.android;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author tim
 */
@Slf4j
public class WifiManagerProxy {
	
	private final Object instance;
	
	private Method createMulticastLock;
	
	private WifiManagerProxy(Object instance) {
		this.instance = instance;
		
		if (instance != null) {
			Class c = instance.getClass();

			try {
				createMulticastLock = c.getMethod("createMulticastLock", String.class);
			} catch (NoSuchMethodException ex) {
				log.error("createMulticastLock() not found in " + c.getName(), ex);
			}
		}
	}
	
	public MulticastLockProxy createMulticastLock(String tag) {
		if (instance == null) {
			return new MulticastLockProxy(null);
		}
		
		try {
			Object lock = createMulticastLock.invoke(instance, tag);
			return new MulticastLockProxy(lock);
		} catch (ReflectiveOperationException ex) {
			log.error("Unable to call createMulticastLock()", ex);
			return new MulticastLockProxy(null);
		}
	}
	
	public static WifiManagerProxy getInstance(Object androidContext) {
		try {
			Class c = Class.forName("android.content.Context");
			String wifiServiceKey = (String) c.getField("WIFI_SERVICE").get(null);
			Method getSystemService = c.getMethod("getSystemService", String.class);
			
			Object wifiManager = getSystemService.invoke(
					androidContext, wifiServiceKey);
			
			return new WifiManagerProxy(wifiManager);
		} catch (ClassNotFoundException ex) {
			log.debug("android.content.Context not found - platform is not android");
			return new WifiManagerProxy(null);
		} catch (ReflectiveOperationException ex) {
			return new WifiManagerProxy(null);
		}
	}
	
	public static class MulticastLockProxy {
		
		private final Object instance;
		
		private Method acquire;
		private Method isHeld;
		private Method release;
		private Method setReferenceCounted;
		private Method toString;

		private MulticastLockProxy(Object instance) {
			this.instance = instance;
			
			if (instance != null) {
				Class c = instance.getClass();

				try {
					acquire = c.getMethod("acquire");
					isHeld = c.getMethod("isHeld");
					release = c.getMethod("release");
					setReferenceCounted = c.getMethod(
							"setReferenceCounted", Boolean.TYPE);
					toString = c.getMethod("toString");
				} catch (NoSuchMethodException ex) {
					log.error("Unable to get method", ex);
				}
			}
		}
		
		public void acquire() {
			if (instance == null) {
				return;
			}
			
			try {
				acquire.invoke(instance);
			} catch (ReflectiveOperationException ex) {
				log.error("Unable to call acquire()", ex);
			}
		}
		
		public boolean isHeld() {
			if (instance == null) {
				return false;
			}
			
			try {
				return (boolean) isHeld.invoke(instance);
			} catch (ReflectiveOperationException ex) {
				log.error("Unable to call isHeld()", ex);
				return false;
			}
		}
		
		public void release() {
			if (instance == null) {
				return;
			}
			
			try {
				release.invoke(instance);
			} catch (ReflectiveOperationException ex) {
				log.error("Unable to call release()", ex);
			}
		}
		
		public void setReferenceCounted(boolean refCounted) {
			if (instance == null) {
				return;
			}
			
			try {
				setReferenceCounted.invoke(instance, refCounted);
			} catch (ReflectiveOperationException ex) {
				log.error("Unable to call setReferenceCounted()", ex);
			}
		}
		
		@Override
		public String toString() {
			try {
				return (String) toString.invoke(instance);
			} catch (ReflectiveOperationException roe) {
				return null;
			}
		}
		
	}
	
}
