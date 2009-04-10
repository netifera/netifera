package com.netifera.platform.net.sniffing;

import com.netifera.platform.net.pcap.ICaptureInterface;


public interface ISnifferHandle {
	void register();
	void unregister();
	ICaptureInterface getInterface();
	void setHighPriority();
	void setDefaultTag(Object tag);
	Object getDefaultTag();
}
