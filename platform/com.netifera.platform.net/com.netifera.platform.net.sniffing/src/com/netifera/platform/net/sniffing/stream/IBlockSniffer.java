package com.netifera.platform.net.sniffing.stream;

import java.nio.ByteBuffer;


public interface IBlockSniffer {
	void handleBlock(ISessionContext ctx, ByteBuffer clientData, ByteBuffer serverData);
}
