package com.netifera.platform.net.sniffing.stream;

import com.netifera.platform.net.sniffing.ISnifferHandle;


public interface IBlockSnifferHandle extends ISnifferHandle, IBlockSnifferConfig {
	
	void setClientLimit(int limit);
	void setServerLimit(int limit);
	void setTotalLimit(int limit);
	int getClientLimit();
	int getServerLimit();
	int getTotalLimit();
	IBlockSniffer getSniffer();


}
