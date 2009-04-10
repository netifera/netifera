package com.netifera.platform.net.wifi.tools;

import java.nio.ByteBuffer;

import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.packets.PacketPayload;
import com.netifera.platform.net.sniffing.IPacketFilter;
import com.netifera.platform.net.wifi.packets.WiFiFrame;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffer;

public class TestWifiSniffer implements IWifiSniffer {
	
	private StringBuffer outputBuffer = new StringBuffer();
	

	
	private static final int MIN_LENGTH = 12;
	
	private void resetBuffer() {
		outputBuffer = new StringBuffer();
	}
	private void processFrame(WiFiFrame frame) {
		//System.out.println("wifi --> " + frame.print());
		PacketPayload payload = (PacketPayload) frame.findHeader(PacketPayload.class);
		
		boolean asciiFound = false;
		if(payload == null) {
			return;
		}
		
		ByteBuffer payloadBuffer = payload.toByteBuffer();
		ByteBuffer lineBuffer = ByteBuffer.allocate(1024);
		
		while(payloadBuffer.remaining() > 0) {
			byte b = payloadBuffer.get();
			if(b >= 32 && b <= 126) {
				if(lineBuffer.remaining() > 0) {
					lineBuffer.put(b);
				}
			} else {
				if(lineBuffer.position() >= MIN_LENGTH) {
					if(!asciiFound) {
						outputBuffer.append(frame.print() + "\n");
					}
					lineBuffer.flip();
					printBuffer(lineBuffer);
					asciiFound = true;
				}
				lineBuffer.clear();
			}
		}
		
		if(asciiFound) {
			outputBuffer.append("\n");
			//ctx.extra("wifi-ascii", outputBuffer.toString());
			resetBuffer();
		}
		
		
	}
	
	private void printBuffer(ByteBuffer buffer) {
		while(buffer.remaining() > 0) {
			outputBuffer.append((char)buffer.get());
		}
		outputBuffer.append("\n");
	}
	
	public void handleWifiFrame(WiFiFrame wifi, IPacketModuleContext ctx) {
		processFrame(wifi);
		
	}
	public IPacketFilter getFilter() {
		return null;
	}
	public String getName() {
		return "Wireless testing module";
	}

}
