package com.netifera.platform.net.packets.link;

import com.netifera.platform.net.packets.IPacketHeader;

public interface EthernetEncapsulable extends IPacketHeader {
	public int protocolOverEthernet();
}
