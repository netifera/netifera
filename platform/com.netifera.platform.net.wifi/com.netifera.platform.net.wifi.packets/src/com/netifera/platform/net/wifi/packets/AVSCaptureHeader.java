package com.netifera.platform.net.wifi.packets;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.PacketException;

public class AVSCaptureHeader extends AbstractPacket {

	@Override
	protected int minimumHeaderLength() {
		/* AVS V1 = 64 bytes */
		/* AVS V2 = 80 bytes */
		return 64;
	}

	@Override
	protected void packHeader() {
		throw new PacketException("Creating AVS headers not implemented");
	}

	@Override
	protected void unpackHeader() {
		throw new PacketException("Decoding AVS headers not implemented");
	}

}
