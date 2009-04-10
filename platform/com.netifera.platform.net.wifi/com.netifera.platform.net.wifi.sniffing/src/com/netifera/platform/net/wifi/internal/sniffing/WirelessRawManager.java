package com.netifera.platform.net.wifi.internal.sniffing;

import java.nio.ByteBuffer;

import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.pcap.ICaptureHeader;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.pcap.ex.ICaptureInterfaceEx;
import com.netifera.platform.net.sniffing.ISniffingEngineService;
import com.netifera.platform.net.sniffing.util.AbstractPacketManager;
import com.netifera.platform.net.sniffing.util.IPacketSource;
import com.netifera.platform.net.wifi.pcap.IWifiPacketCapture;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingEngine;

public class WirelessRawManager extends AbstractPacketManager<IPacketHeader> implements IPacketSource {

	private IPacketDecoder decoder;
	private IWifiPacketCapture wirelessPcap;
	private Thread sniffingThread;
	private final Runnable sniffingLoop;
	private final IPacketHandler handler;
	private final ICaptureInterfaceEx iface;
	
	public WirelessRawManager(IWifiSniffingEngine engine, ICaptureInterfaceEx iface) {
		super(engine, iface);
		this.iface = iface;
		sniffingLoop = new Runnable() {
			public void run() {
				while(!Thread.interrupted())
					readPackets();				
			}
		};
		
		handler = new IPacketHandler() {

			public void handlePacket(ByteBuffer packetData,
					ICaptureHeader header) {
				processPacket(packetData, header);				
			}
			
		};
	}
	
	private void processPacket(ByteBuffer packetData, ICaptureHeader header) {
		if(!hasClients()) 
			return;
		
		final IPacketHeader packet = decoder.decode(packetData);
		deliverPacket(packet, header);
	}
	
	private void readPackets() {
		if(!wirelessPcap.read()) {
			getLogger().error("Wireless read failed on interface (" +
					getInterface().getName() + ") : " + wirelessPcap.getLastError());
			getLogger().warning("Closing interface (" + getInterface().getName() + ") on error.");
			wirelessPcap.close();
			wirelessPcap = null;
			Thread.currentThread().interrupt();
		}
	}
	@Override
	protected boolean start() {
		if(isStarted())
			return true;
		
		final ISniffingEngineService e = getSniffingEngine();
		final IPacketCapture pcap = iface.pcapCreate(e.getSnaplen(), e.getPromiscuous(), e.getTimeout(), handler);
		if(!(pcap instanceof IWifiPacketCapture)) {
			getLogger().error("Unexpected capture instance type in Wireless Raw Manager");
			return false;
		}
		
		if(!pcap.open()) {
			getLogger().error("Failed to open wireless interface '"
					+ getInterface().getName() + "' for sniffing: "
					+ pcap.getLastError());
			pcap.close();
			return false;
		}
		
		decoder = pcap.getDecoder();
		if(decoder == null) {
			getLogger().error("No decoder available for link type " + pcap.getLinkType());
			pcap.close();
			return false;
		}
		wirelessPcap = (IWifiPacketCapture) pcap;
		sniffingThread = new Thread(sniffingLoop, "Wireless Sniffing Engine Thread on Interface (" +
				getInterface().getName() + ")");
		sniffingThread.start();
		return true;
	}

	@Override
	protected void stop() {
		sniffingThread.interrupt();
		if(wirelessPcap != null) {
			wirelessPcap.close();
		}
		try {
			sniffingThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		sniffingThread = null;
		wirelessPcap = null;
		
	}

}
