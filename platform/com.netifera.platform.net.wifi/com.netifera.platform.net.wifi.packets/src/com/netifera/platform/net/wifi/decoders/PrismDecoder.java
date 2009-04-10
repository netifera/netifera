package com.netifera.platform.net.wifi.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.wifi.packets.AVSCaptureHeader;
import com.netifera.platform.net.wifi.packets.PrismCaptureHeader;

public class PrismDecoder implements IPacketDecoder {
	private final static IPacketDecoder wifiDecoder = new WiFiDecoder();
	private static final int WLANCAP_MAGIC_COOKIE_V1 = 0x80211001;
	private static final int WLANCAP_MAGIC_COOKIE_V2 = 0x80211002;
	private static final int PRISM_COOKIE = 0x44;
	
	public IPacketHeader decode(ByteBuffer buffer) {
		final ByteBuffer prismBuffer = buffer.slice();
		final int cookie = prismBuffer.getInt(0);
		final IPacketHeader header;
		
		if(cookie == WLANCAP_MAGIC_COOKIE_V1 || cookie == WLANCAP_MAGIC_COOKIE_V2) {
			header = new AVSCaptureHeader();
		} else if(AbstractPacket.swap32(cookie) == PRISM_COOKIE) {
			header = new PrismCaptureHeader();
		} else {
			header = null;
		}
		
		if(header != null && header.unpack(prismBuffer)) {
			header.setNextPacket( wifiDecoder.decode(prismBuffer));
			return header;
		} else {
			return new PacketPayload(prismBuffer);
		}
		
	}
//	private static final int WLANCAP_MAGIC_COOKIE_V1 = 0x80211001;
//	private static final int WLANCAP_MAGIC_COOKIE_V2 = 0x80211002;
//	
//	private static final PrismDecoder instance;
//	static {
//		instance = new PrismDecoder();
//		instance.put(PrismCaptureHeader.class, null, WiFiDecoder.defaultInstance());
//		instance.put(AVSCaptureHeader.class, null, WiFiDecoder.defaultInstance());
//	}
//	
//	static public PrismDecoder defaultInstance() { 
//		return instance;
//	}
//	
//	
//	public IPacket decode(byte[] data, int offset, int length) {
//		int cookie = FieldPacker.bufferUnpack32(data, offset);
//		if(cookie == WLANCAP_MAGIC_COOKIE_V1 || cookie == WLANCAP_MAGIC_COOKIE_V2) {
//			return decode(data, offset, length, AVSCaptureHeader.class);			
//		} else {
//			if(Bytesex.swap32(cookie) != 0x44) return null;
//			return decode(data, offset, length , PrismCaptureHeader.class);
//		}
//	}

}
