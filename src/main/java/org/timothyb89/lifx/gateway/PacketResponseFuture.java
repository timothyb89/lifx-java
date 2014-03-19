package org.timothyb89.lifx.gateway;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
			cachedValue = queue.take();
		}
		
		return response;
	}

	@Override
	public PacketResponse get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (cachedValue == null) {
			cachedValue = queue.poll(timeout, unit);
			
			if (cachedValue == null) {
				throw new TimeoutException();
			}
		}
		
		return response;
	}
	
	public boolean expectsResponse(int packetType) {
		return response.isExpecting(packetType);
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
	
}
