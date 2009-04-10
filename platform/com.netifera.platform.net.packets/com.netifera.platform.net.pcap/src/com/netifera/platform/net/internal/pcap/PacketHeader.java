package com.netifera.platform.net.internal.pcap;

import com.netifera.platform.net.pcap.ICaptureHeader;

public class PacketHeader implements ICaptureHeader {
	private final int seconds;
	private final int useconds;
	private final int caplen;
	private final int datalen;
	
	public PacketHeader(int seconds, int useconds, int caplen, int datalen) {
		this.seconds = seconds;
		this.useconds = useconds;
		this.caplen = caplen;
		this.datalen = datalen;
	}
	public int getCaplen() {
		return caplen;
	}

	public int getDatalen() {
		return datalen;
	}

	public int getMicroseconds() {
		return useconds;
	}

	public long getSeconds() {
		return seconds;
	}

}
