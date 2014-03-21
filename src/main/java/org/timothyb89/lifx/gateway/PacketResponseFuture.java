package org.timothyb89.lifx.gateway;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.timothyb89.lifx.net.field.MACAddress;
import org.timothyb89.lifx.net.packet.Packet;

/**
 *
 * @author tim
 */
public class PacketResponseFuture implements Future<PacketResponse> {
	
	private BlockingQueue<PacketResponse> queue;

	private PacketResponse response;
	private PacketResponse cachedValue;
	
	public PacketResponseFuture(PacketResponse response) {
		this.response = response;
		
		queue = new ArrayBlockingQueue<>(1);
		
		// empty responses are always "fulfilled"
		//if (response.isFulfilled()) {
		//	queue.offer(response);
		//}
		// disabled to allow use of expects()
		
		cachedValue = null;
	}
	
	public PacketResponseFuture(Packet packet) {
		this(new PacketResponse(packet));
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException("Not cancellable");
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return !queue.isEmpty();
	}

	@Override
	public PacketResponse get() throws InterruptedException, ExecutionException {
		if (cachedValue == null) {
			if (response.isFulfilled()) {
				// self-fulfil if we're not expecting anything
				// (avoid blocking)
				cachedValue = response;
			} else {
				// otherwise, wait
				cachedValue = queue.take();
			}
		}
		
		return response;
	}

	@Override
	public PacketResponse get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (cachedValue == null) {
			if (response.isFulfilled()) {
				cachedValue = response;
			} else {
				cachedValue = queue.poll(timeout, unit);

				if (cachedValue == null) {
					throw new TimeoutException();
				}
			}
		}
		
		return response;
	}
	
	public boolean expectsResponse(int packetType) {
		return response.isExpecting(packetType);
	}
	
	public boolean expectsResponse(int packetType, MACAddress address) {
		return response.isExpecting(packetType, address);
	}
	
	public void putResponse(Packet packet) {
		response.addResponse(packet);
		
		if (response.isFulfilled()) {
			queue.offer(response);
		}
	}
	
	public boolean isFulfilled() {
		return response.isFulfilled();
	}
	
	/**
	 * @see PacketResponse#expect(int)
	 * @param packetType the type to expect
	 * @return this
	 */
	public PacketResponseFuture expect(int packetType) {
		response.expect(packetType);
		return this;
	}
	
	/**
	 * @see PacketResponse#expect(int, MACAddress) 
	 * @param packetType the packet type to expect
	 * @param address the address to expect
	 * @return this
	 */
	public PacketResponseFuture expect(int packetType, MACAddress address) {
		response.expect(packetType, address);
		return this;
	}
	
}
