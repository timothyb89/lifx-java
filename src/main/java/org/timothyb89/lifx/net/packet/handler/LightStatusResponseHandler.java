package org.timothyb89.lifx.net.packet.handler;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.response.LightStatusResponse;

/**
 *
 * @author tim
 */
public class LightStatusResponseHandler implements PacketHandler {

	@Override
	public Packet handle(ByteBuffer buf) {
		LightStatusResponse packet = new LightStatusResponse();
		packet.parse(buf);
		
		return packet;
	}
	
}
