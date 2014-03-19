package org.timothyb89.lifx.net.packet.handler;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.response.PowerStateResponse;

/**
 *
 * @author tim
 */
public class PowerStateResponseHandler implements PacketHandler {

	@Override
	public Packet handle(ByteBuffer buf) {
		PowerStateResponse ret = new PowerStateResponse();
		ret.parse(buf);
		
		return ret;
	}
	
}
