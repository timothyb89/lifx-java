package org.timothyb89.lifx.net.packet.handler;

import java.nio.ByteBuffer;
import org.timothyb89.lifx.net.packet.Packet;
import org.timothyb89.lifx.net.packet.response.PANGatewayResponse;

/**
 *
 * @author tim
 */
public class PANGatewayResponseHandler implements PacketHandler {

	@Override
	public Packet handle(ByteBuffer buf) {
		PANGatewayResponse ret = new PANGatewayResponse();
		ret.parse(buf);
		
		return ret;
	}
	
}
