package com.netifera.platform.net.packets.link;

import java.util.Arrays;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.util.NetworkConstants;
import com.netifera.platform.util.addresses.IHardwareAddress;
import com.netifera.platform.util.addresses.INetworkAddress;
import com.netifera.platform.util.addresses.MACAddress;
import com.netifera.platform.util.addresses.inet.IPv4Address;
import com.netifera.platform.util.addresses.inet.IPv6Address;

public class ARP extends AbstractPacket implements EthernetEncapsulable {

	/**
	 *     0       1       2       3       4       5       6       7  
	 * +-------+-------+-------+-------+-------+-------+-------+-------+
	 * | Hardware Type | Protocol Type |HW Size|Pr Size|  Operation    |
	 * +-------+-------+-------+-------+-------+-------+-------+-------+
	 * | Sender Hardware Address     ..|.. Sender Protocol Address   ..|
	 * +-------+-------+-------+-------+-------+-------+-------+-------+
	 * |.. Target Hardware Address   ..|.. Target Protocol Address   ..|
	 * +-------+-------+-------+-------+-------+-------+-------+-------+
	 */
	
	private int hardwareType;
	private int protocolType;
	private int hardwareSize;
	private int protocolSize;
	private int operation;
	private byte[] senderHardwareAddressBytes;
	private byte[] senderProtocolAddressBytes;
	private byte[] targetHardwareAddressBytes;
	private byte[] targetProtocolAddressBytes;
	
	private boolean unpackFailed;
	
	@Override
	protected int minimumHeaderLength() {
		return 8;
	}

	@Override
	protected void packHeader() {
		pack16(hardwareType);
		pack16(protocolType);
		pack8(hardwareSize);
		pack8(protocolSize);
		pack16(operation);
		packBytes(senderHardwareAddressBytes);
		packBytes(senderProtocolAddressBytes);
		if (targetHardwareAddressBytes == null) {
			targetHardwareAddressBytes = MACAddress.ANY.toBytes();
		}
		packBytes(targetHardwareAddressBytes);
		packBytes(targetProtocolAddressBytes);
	}

	@Override
	protected void unpackHeader() {
		hardwareType = unpack16();
		protocolType = unpack16();
		hardwareSize = unpack8();
		protocolSize = unpack8();
		operation = unpack16();

		if(remaining() < (2 * hardwareSize) + (2 * protocolSize)) {
			unpackFailed = true;
			return;
		}
		
		senderHardwareAddressBytes = unpackBytes(hardwareSize);
		senderProtocolAddressBytes = unpackBytes(protocolSize);
		targetHardwareAddressBytes = unpackBytes(hardwareSize);
		targetProtocolAddressBytes = unpackBytes(protocolSize);
	}
	
	protected boolean isValidHeader() {
		return !unpackFailed;
	}
	
	public ARP() { }
		
	public ARP(IPacketHeader pkt) {
		super(pkt);
	}

	public IHardwareAddress getSenderHardwareAddress() {
		if (hardwareType == NetworkConstants.ETHER_TYPE)
			return new MACAddress(senderHardwareAddressBytes);
		throw new RuntimeException("Unknown address type "+hardwareType);
	}
	
	public void setSenderHardwareAddress(MACAddress address) {
		senderHardwareAddressBytes = address.toBytes();
		setHardwareEthernet();
	}
	
	public IHardwareAddress getTargetHardwareAddress() {
		if (hardwareType == NetworkConstants.ETHER_TYPE)
			return new MACAddress(targetHardwareAddressBytes);
		throw new RuntimeException("Unknown hardware type "+hardwareType);
	}

	public void setTargetHardwareAddress(MACAddress address) {
		targetHardwareAddressBytes = address.toBytes();
		setHardwareEthernet();
	}
	
	public INetworkAddress getSenderProtocolAddress() {
		if (protocolType == NetworkConstants.ETHERTYPE_IPv4)
			return new IPv4Address(senderProtocolAddressBytes);
		else if (protocolType == NetworkConstants.ETHERTYPE_IPv6)
			return new IPv6Address(senderProtocolAddressBytes);
		throw new RuntimeException("Unknown protocol type "+protocolType);
	}
	
	public void setSenderProtocolAddress(INetworkAddress address) {
		senderProtocolAddressBytes = address.toBytes();
		setProtocolIP();
	}
	
	public INetworkAddress getTargetProtocolAddress() {
		if (protocolType == NetworkConstants.ETHERTYPE_IPv4)
			return new IPv4Address(targetProtocolAddressBytes);
		else if (protocolType == NetworkConstants.ETHERTYPE_IPv6)
			return new IPv6Address(targetProtocolAddressBytes);
		throw new RuntimeException("Unknown protocol type "+protocolType);
	}
	
	public void setTargetProtocolAddress(INetworkAddress address) {
		targetProtocolAddressBytes = address.toBytes();
		if (address instanceof IPv6Address)
			setProtocolIPv6();
		else
			setProtocolIP();
	}
	
	private void setHardwareEthernet() {
		hardwareType = NetworkConstants.ETHER_TYPE;
		hardwareSize = 6;
	}

	private void setProtocolIP() {
		protocolType = NetworkConstants.ETHERTYPE_IPv4;
		protocolSize = 4;
	}

	private void setProtocolIPv6() {
		protocolType = NetworkConstants.ETHERTYPE_IPv6;
		protocolSize = 16;
	}

	void setOperation(int operation) {
		this.operation = operation;
	}
	
	public static ARP whoHas(INetworkAddress targetProtocolAddress, MACAddress senderHardwareAddress, INetworkAddress senderProtocolAddress) {
		ARP arp = new ARP(PacketPayload.emptyPayload());
		arp.setSenderHardwareAddress(senderHardwareAddress);
		arp.setSenderProtocolAddress(senderProtocolAddress);
		arp.setTargetProtocolAddress(targetProtocolAddress);
		arp.setOperation(NetworkConstants.ARPOP_REQUEST);
		return arp;
	}
	
	public static ARP isAt(INetworkAddress targetProtocolAddress, MACAddress targetHardwareAddress, INetworkAddress senderProtocolAddress, MACAddress senderHardwareAddress) {
		ARP arp = new ARP(PacketPayload.emptyPayload());
		arp.setSenderHardwareAddress(senderHardwareAddress);
		arp.setSenderProtocolAddress(senderProtocolAddress);
		arp.setTargetHardwareAddress(targetHardwareAddress);
		arp.setTargetProtocolAddress(targetProtocolAddress);
		arp.setOperation(NetworkConstants.ARPOP_REPLY);
		return arp;
	}	

	@Override
	public int headerLength() {
		return 8 + hardwareSize*2 + protocolSize*2;
	}

	public boolean isRequest() {
		return operation == NetworkConstants.ARPOP_REQUEST;
	}
	
	public boolean isReply() {
		return operation == NetworkConstants.ARPOP_REPLY;
	}
	
	// Answer true if this is a gratuitous ARP
	public boolean isAnnouncement() {
		return Arrays.equals(targetProtocolAddressBytes, senderProtocolAddressBytes);
	}

	@Override
	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("ARP ");
		if (isRequest()) {
			out.append(" who-has ");
			out.append(getTargetProtocolAddress());
			out.append(" tell ");
			out.append(getSenderProtocolAddress());
		} else if (isReply()) {
			out.append(getSenderProtocolAddress());
			out.append(" is-at ");
			out.append(getSenderHardwareAddress());
		} else {
			out.append(" (opcode ");
			out.append(operation);
			out.append(")");
		}
		return out.toString();
	}

	public int protocolOverEthernet() {
		return NetworkConstants.ETHERTYPE_ARP;
	}
}
