package com.netifera.platform.net.packets.link;

import com.netifera.platform.net.packets.AbstractPacket;

/*
 * Logical Link Control
 * 
 * (Layer2: Data Link)
 * 
 */
public class LLC extends AbstractPacket {
	private int dsap = 0xaa;
	private int ssap = 0xaa;
	private int control = 0;
	private int protocol = 0;
	private int type;

	@Override
	protected void packHeader() {
		pack8(dsap);
		pack8(ssap);
		pack8(control);
		pack8(protocol & 0xff);
		pack8((protocol >> 8) & 0xff);
		pack8((protocol >> 16) & 0xff);
		pack16(type);
	}

	@Override
	protected void unpackHeader() {
		dsap = unpack8();
		ssap = unpack8();
		control = unpack8();
		protocol = unpack8();
		protocol = protocol*0x100 + unpack8();
		protocol = protocol*0x100 + unpack8();
		type = unpack16();
	}
	
	public LLC() {}
	
	public LLC(EthernetEncapsulable payload) {
		super(payload);
		type = payload.protocolOverEthernet();
	}
	
	@Override
	public int minimumHeaderLength() {
		return 8;
	}

	@Override
	public int headerLength() {
		return 8;
	}
	
	public int length() {
		return remaining();
	}
	
	@Override
	public int nextProtocol() {
		return type;
	}
	
	@Override
	public String toString() {
		return "LLC ("+type+")";
	}
}
