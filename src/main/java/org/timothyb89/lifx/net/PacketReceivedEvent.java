package org.timothyb89.lifx.net;

import java.net.InetSocketAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.timothyb89.eventbus.Event;
import org.timothyb89.lifx.net.packet.Packet;

/**
 * Event pushed when a packet has been received from a BroadcastListener.
 * @author timothyb89
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PacketReceivedEvent extends Event {
	
	private final BroadcastListener listener;
	private final InetSocketAddress source;
	private final Packet packet;
	
}
