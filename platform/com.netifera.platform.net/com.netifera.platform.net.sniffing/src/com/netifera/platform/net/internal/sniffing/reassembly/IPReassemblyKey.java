package com.netifera.platform.net.internal.sniffing.reassembly;

import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.util.addresses.inet.InternetAddress;

class IPReassemblyKey {
	private final int version;
	private final InternetAddress source;
	private final InternetAddress destination;
	private final int protocol;
	private final int id;
	
	public IPReassemblyKey(IP packet) {
		this.version = packet.getVersion();
		this.source = packet.getSourceAddress();
		this.destination = packet.getDestinationAddress();
		this.protocol = packet.getNextProtocol();
		this.id = packet.fragment().getIdentification();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null || !(obj instanceof IPReassemblyKey)){
			return false;
		}
		IPReassemblyKey key = (IPReassemblyKey)obj;
		return key.version == version
			&& key.id == id
			&& key.protocol == protocol
			&& key.source.equals(source)
			&& key.destination.equals(destination);
	}
	
	@Override
	public int hashCode() {
		return id ^ source.hashCode();
	}
}
