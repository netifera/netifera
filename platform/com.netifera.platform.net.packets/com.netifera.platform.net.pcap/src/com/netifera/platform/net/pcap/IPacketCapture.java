package com.netifera.platform.net.pcap;

import java.util.List;

import com.netifera.platform.net.packets.IPacketDecoder;

public interface IPacketCapture {
	enum PcapDirection { PCAP_D_INOUT, PCAP_D_IN, PCAP_D_OUT };
	boolean open();
	void close();
	boolean read();
	int getFileDescriptor();
	Datalink getLinkType();
	boolean setDataLink(Datalink dlt);
	List<Datalink> getDltList();
	String getLastError();
	void setError(String error);
	IPacketDecoder getDecoder();

}
