package com.netifera.platform.net.wifi.packets;

import java.nio.ByteOrder;

import com.netifera.platform.net.packets.AbstractPacket;
import com.netifera.platform.net.packets.PacketException;

public class PrismCaptureHeader extends AbstractPacket {

	private final boolean nativeBigEndian = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);

	
	/*
	 * From wireshark packet-prism.c:
	 * 
	 * It appears from looking at the linux-wlan-ng and Prism II HostAP
	 * drivers, and various patches to the orinoco_cs drivers to add
	 * Prism headers, that:
	 *
	 *	the "did" identifies what the value is (i.e., what it's the value
	 *	of);
	 *
	 *	"status" is 0 if the value is present or 1 if it's absent;
	 *
	 *	"len" is the length of the value (always 4, in that code);
	 *
	 *	"data" is the value of the data (or 0 if not present).
	 *
	 * Note: all of those values are in the *host* byte order of the machine
	 * on which the capture was written.
	 */
	
	private int unpackField() {
		int did = unpack32();
		int status = unpack16();
		int length = unpack16();
		int data = unpack32();
		
		if(!nativeBigEndian) {
			did = swap32(did);
			status = swap16(status);
			length = swap16(length);
			data = swap32(data);
		}
		
		if(status == 0) {
			return data;
		} else {
			return -1;
		}
		
	}
	
	
	@Override
	protected int minimumHeaderLength() {
		/* Prism = 144 bytes */
		return 144;
	}
	
	private int msgcode;
	private int msglen;
	private byte[] devname;
	private int hosttime = -1;
	private int mactime = -1;
	private int channel = -1;
	private int rssi = -1;
	private int sq = -1;
	private int signal = -1;
	private int noise = -1;
	private int rate = -1;
	private int istx = -1;
	private int frmlen = -1;
	
	
	@Override
	protected void packHeader() {
		throw new PacketException("Creating Prism Headers not implemented");
	}

	
	@Override
	protected void unpackHeader() {
		msgcode = unpack32();
		msglen = unpack32();
		
		if(!nativeBigEndian) {
			msgcode = swap32(msgcode);
			msglen = swap32(msglen);
		}
		
		devname = unpackBytes(16);
		hosttime = unpackField();
		mactime = unpackField();
		channel = unpackField();
		rssi = unpackField();
		sq = unpackField();
		signal = unpackField();
		noise = unpackField();
		rate = unpackField();
		istx = unpackField();
		frmlen = unpackField();
	}
	
	
	public int msgcode() {
		return msgcode;
	}
	
	public int msglen() {
		return msglen;
	}
	
	public byte[] devname() {
		return devname;
	}
	
	public int hosttime() {
		return hosttime;
	}
	public int mactime() {
		return mactime;
	}
	
	public int channel() {
		return channel;
	}
	
	public int rssi() {
		return rssi;
	}
	
	public int sq() {
		return sq;
	}
	
	public int signal() {
		return signal;
	}
	
	public int noise() {
		return noise;
	}
	
	public int rate() {
		return rate;
	}
	
	public int istx() {
		return istx;
	}
	
	public int frmlen() {
		return frmlen;
	}

}
