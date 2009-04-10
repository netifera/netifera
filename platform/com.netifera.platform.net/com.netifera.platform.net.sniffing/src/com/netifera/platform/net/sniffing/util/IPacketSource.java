package com.netifera.platform.net.sniffing.util;

import com.netifera.platform.net.internal.sniffing.managers.IPacketManager;
import com.netifera.platform.net.packets.IPacketHeader;

public interface IPacketSource extends IPacketManager<IPacketHeader> {
	// void setPacketTag(Object tag);
}
