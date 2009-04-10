package com.netifera.platform.net.pcap.ex;

import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketHandler;

public interface ICaptureInterfaceEx extends ICaptureInterface {
	IPacketCapture pcapCreate(int snaplen, boolean promiscuous, int timeout, IPacketHandler handler);
}
