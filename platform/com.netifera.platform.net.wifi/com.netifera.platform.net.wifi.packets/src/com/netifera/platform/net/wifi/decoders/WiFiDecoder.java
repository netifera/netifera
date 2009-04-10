package com.netifera.platform.net.wifi.decoders;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.packets.decoders.LLCDecoder;
import com.netifera.platform.net.wifi.packets.ATIM;
import com.netifera.platform.net.wifi.packets.Ack;
import com.netifera.platform.net.wifi.packets.AssociationRequest;
import com.netifera.platform.net.wifi.packets.AssociationResponse;
import com.netifera.platform.net.wifi.packets.Beacon;
import com.netifera.platform.net.wifi.packets.CTS;
import com.netifera.platform.net.wifi.packets.ControlFrame;
import com.netifera.platform.net.wifi.packets.DataFrame;
import com.netifera.platform.net.wifi.packets.Deauthentication;
import com.netifera.platform.net.wifi.packets.Disassociation;
import com.netifera.platform.net.wifi.packets.PSPoll;
import com.netifera.platform.net.wifi.packets.ProbeRequest;
import com.netifera.platform.net.wifi.packets.ProbeResponse;
import com.netifera.platform.net.wifi.packets.RTS;
import com.netifera.platform.net.wifi.packets.WiFiFrame;


public class WiFiDecoder implements IPacketDecoder  {

	private final static IPacketDecoder llcDecoder = new LLCDecoder();
	
	public IPacketHeader decode(ByteBuffer buffer) {
		final ByteBuffer wifiBuffer = buffer.slice();
		final int x = wifiBuffer.get(0);
		final int mainType = (x & 0x0c) >> 2;
		final int subType = (x & 0xf0) >> 4;
		final int type = (mainType << 4) | subType;
		
		if(mainType == WiFiFrame.MGT_FRAME) {
			return decodeManagement(type, wifiBuffer);
		} else if(mainType == WiFiFrame.DATA_FRAME) {
			return decodeData(wifiBuffer);
		} else if(mainType == WiFiFrame.CONTROL_FRAME) {
			return decodeControl(type, wifiBuffer);
		}
		
		return decodeManagement(type, wifiBuffer);
	
	}
	
	private IPacketHeader decodeData(ByteBuffer buffer) {
		DataFrame dataFrame = new DataFrame();
		if(!dataFrame.unpack(buffer)) {
			return new PacketPayload(buffer);
		}
		dataFrame.setNextPacket( llcDecoder.decode(buffer) );
		return dataFrame;
	}
	
	private IPacketHeader decodeManagement(int type, ByteBuffer buffer) {
		final IPacketHeader header = getManagementHeader(type);
		
		if(header != null && header.unpack(buffer)) {
			return header;
		} else {
			return new PacketPayload(buffer);
		}		
	}
	
	private IPacketHeader decodeControl(int type, ByteBuffer buffer) {
		final IPacketHeader header = getControlHeader(type);
		
		if(header != null && header.unpack(buffer)) {
			return header;
		} else {
			return new PacketPayload(buffer);
		}	
	}
	
	private IPacketHeader getManagementHeader(int type) {
		
		switch(type) {
		
		case WiFiFrame.MGT_ASSOC_REQ:
		case WiFiFrame.MGT_REASSOC_REQ:
			return new AssociationRequest();
			
		case WiFiFrame.MGT_ASSOC_RESP:
		case WiFiFrame.MGT_REASSOC_RESP:
			return new AssociationResponse();
			
		case WiFiFrame.MGT_PROBE_REQ:
			return new ProbeRequest();
			
		case WiFiFrame.MGT_PROBE_RESP:
			return new ProbeResponse();

		case WiFiFrame.MGT_BEACON:
			return new Beacon();
			
		case WiFiFrame.MGT_ATIM:
			return new ATIM();
			
		case WiFiFrame.MGT_DISASS:
			return new Disassociation();
			
		case WiFiFrame.MGT_DEAUTHENTICATION:
			return new Deauthentication();
			
		default:
			return null;
		}		
	}
	
	private IPacketHeader getControlHeader(int type) {
		
		switch(type) {
		
		case WiFiFrame.CTRL_PS_POLL:
			return new PSPoll();
			
		case WiFiFrame.CTRL_RTS:
			return new RTS();
			
		case WiFiFrame.CTRL_CTS:
			return new CTS();
			
		case WiFiFrame.CTRL_ACKNOWLEDGEMENT:
			return new Ack();
			
		default:
			return new ControlFrame();
		}
	}
//	private static final Map<Integer, Class<? extends WiFiFrame>> typeMap;
//	private static final WiFiDecoder instance;
//
//	static {
//		typeMap = new HashMap<Integer, Class<? extends WiFiFrame>>();
//		typeMap.put(WiFiFrame.MGT_ASSOC_REQ, AssociationRequest.class);
//		typeMap.put(WiFiFrame.MGT_ASSOC_RESP, AssociationResponse.class);
//		typeMap.put(WiFiFrame.MGT_REASSOC_REQ, AssociationRequest.class);
//		typeMap.put(WiFiFrame.MGT_REASSOC_RESP, AssociationResponse.class);
//		typeMap.put(WiFiFrame.MGT_PROBE_REQ, ProbeRequest.class);
//		typeMap.put(WiFiFrame.MGT_PROBE_RESP, ProbeResponse.class);
//		typeMap.put(WiFiFrame.MGT_BEACON, Beacon.class);
//		typeMap.put(WiFiFrame.MGT_ATIM, ATIM.class);
//		typeMap.put(WiFiFrame.MGT_DISASS, Disassociation.class);
//		typeMap.put(WiFiFrame.MGT_AUTHENTICATION, Authentication.class);
//		typeMap.put(WiFiFrame.MGT_DEAUTHENTICATION, Deauthentication.class);
//		typeMap.put(WiFiFrame.CTRL_PS_POLL, PSPoll.class);
//		typeMap.put(WiFiFrame.CTRL_RTS, RTS.class);
//		typeMap.put(WiFiFrame.CTRL_CTS, CTS.class);
//		typeMap.put(WiFiFrame.CTRL_ACKNOWLEDGEMENT, Ack.class);
//		// typeMap.put(WiFiFrame.CTRL_CFP_END, ControlFrame.class);
//		// typeMap.put(WiFiFrame.CTRL_CFP_ENDACK, ControlFrame.class);
//		
//		instance = new WiFiDecoder();
//		instance.put(DataFrame.class, 0, LLCDecoder.defaultInstance());
//	}
//
//	static public WiFiDecoder defaultInstance() {
//		return instance;
//	}
//
//	public WiFiDecoder() {
//		super();
//	}
//
//	public IPacket decode(byte[] data, int offset, int length) {
//		int x = data[offset];
////		int version = x & 0x03;
//		int mainType = (x & 0x0c) >> 2;
//		int subType = (x & 0xf0) >> 4;
//		int type = (mainType << 4) | subType;
//		
////		System.out.println("wifi: "+hexa(data, offset));
////		System.out.println(String.format("%x %x %x", type, mainType, subType));
//		Class<? extends WiFiFrame> packetClass = typeMap.get(type);
//		if (packetClass == null) {
//			switch (mainType) {
//				case WiFiFrame.CONTROL_FRAME: packetClass = ControlFrame.class;
//					break;
//				case WiFiFrame.DATA_FRAME: packetClass = DataFrame.class;
//					break;
//				case WiFiFrame.MGT_FRAME: packetClass = ManagementFrame.class;
//					break;
//				default:
//					throw new PacketDecodeException("Unknown frame type "+type);
//			}
//		}
//		return decode(data, offset, length, packetClass);
//	}
//	
	// XXX
/*	private String hexa(byte[] bytes, int offset) {
		final String xlat = "0123456789abcdef";
		String answer = "";
		for (int i=offset; i<bytes.length; i++) {
			byte x = bytes[i];
			answer += String.valueOf(xlat.charAt((x >>> 4) & 0x0F)) + String.valueOf(xlat.charAt(x & 0x0F));
		}
		return answer;		
	}*/
}