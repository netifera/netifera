package com.netifera.platform.net.sniffing.stream;

import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.sniffing.ISnifferHandle;


public interface IStreamSnifferHandle extends ISnifferHandle {	
	IPacketFilter getFilter();
	IStreamSniffer getSniffer();
}
