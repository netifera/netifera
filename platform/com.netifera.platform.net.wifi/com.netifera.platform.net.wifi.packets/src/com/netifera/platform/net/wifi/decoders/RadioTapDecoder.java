package com.netifera.platform.net.wifi.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.wifi.packets.RadioTapHeader;

public class RadioTapDecoder implements IPacketDecoder  {
	private final static IPacketDecoder wifiDecoder = new WiFiDecoder();

	public IPacketHeader decode(ByteBuffer buffer) {
		final ByteBuffer rtapBuffer = buffer.slice();
		final RadioTapHeader radioTap = new RadioTapHeader();
		
		if(!radioTap.unpack(rtapBuffer)) {
			return new PacketPayload(buffer.slice());
		}
		
		radioTap.setNextPacket( wifiDecoder.decode(rtapBuffer) );
		
		return radioTap;
	}

//	private static final RadioTapDecoder instance;
//	static {
//		instance = new RadioTapDecoder();
//		instance.put(RadioTapHeader.class, null, WiFiDecoder.defaultInstance());
//	}
//	
//	static public void test() {
//		// XXX TEST
//		byte[] packet = {0x00, 0x00, 0x19, 0x00, 0x6f, 0x08, 0x00, 0x00, 0x61, (byte)0x8d, 0x00, (byte)0xd0,
//				0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0x85, 0x09, (byte)0xa0, 0x00, (byte)0xbb, 0x00, 0x02, (byte)0x80, 0x00, 0x00,
//				0x00, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 0x00, 0x17, (byte)0x9a, 0x68, (byte)0xe9, (byte)0x85, 0x00, 0x17, (byte)0x9a,
//				0x68, (byte)0xe9, (byte)0x85, 0x70, 0x72, (byte)0x83, (byte)0xe1, (byte)0x9b, (byte)0xff, 0x01, 0x00, 0x00, 0x00, 0x64, 0x00, 0x31,
//				0x04, 0x00, 0x05, 0x64, 0x6c, 0x69, 0x6e, 0x6b, 0x01, 0x04, (byte)0x82, (byte)0x84, (byte)0x8b, (byte)0x96, 0x03, 0x01,
//				0x06, (byte)0xdd, 0x16, 0x00, 0x50, (byte)0xf2, 0x01, 0x01, 0x00, 0x00, 0x50, (byte)0xf2, 0x02, 0x01, 0x00, 0x00};
//		System.out.println(instance.decode(packet, packet.length).print());
//		System.out.println("23:27:47.326436 3489697121us tsft 1.0 Mb/s 2437 MHz (0x00a0) -69dB signal 0dB noise antenna 2 0us BSSID:00:17:9a:68:e9:85 DA:ff:ff:ff:ff:ff:ff SA:00:17:9a:68:e9:85 Beacon (dlink) [1.0* 2.0* 5.5* 11.0* Mbit] ESS CH: 6, PRIVACY");
//	}
//	
//	static public RadioTapDecoder defaultInstance() {return instance;}
//	
//	public RadioTapDecoder() {
//		super();
//	}
//
//
//	public IPacket decode(byte[] data, int offset, int length) {
////		System.out.println("\nrtap: "+hexa(data, offset));
//		return decode(data, offset, length, RadioTapHeader.class);
//	}
//	
//	// XXX
//	@SuppressWarnings("unused")
//	private String hexa(byte[] bytes, int offset) {
//		final String xlat = "0123456789abcdef";
//		String answer = "";
//		for (int i=offset; i<bytes.length; i++) {
//			byte x = bytes[i];
//			answer += String.valueOf(xlat.charAt((x >>> 4) & 0x0F)) + String.valueOf(xlat.charAt(x & 0x0F));
//		}
//		return answer;		
//	}
}
