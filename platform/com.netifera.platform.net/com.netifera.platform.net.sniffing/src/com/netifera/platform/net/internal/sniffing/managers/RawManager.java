package com.netifera.platform.net.internal.sniffing.managers;

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
import com.netifera.platform.net.sniffing.util.ISniffingEngineEx;

public class RawManager extends AbstractPacketManager<IPacketHeader> implements IPacketSource {

	private IPacketDecoder decoder;
	private IPacketCapture pcap;
	// private final IPReassembler ipReassembler = new IPReassembler();

	private Thread sniffingThread;

	private final Runnable sniffingLoop;
	private final IPacketHandler handler;
	private final ICaptureInterfaceEx iface;
	
	public RawManager(ISniffingEngineEx engine, ICaptureInterfaceEx captureInterface) {
		super(engine, captureInterface);
		
		iface = captureInterface;
		sniffingLoop = new Runnable() {
			
			public void run() {
				while(!Thread.interrupted()) {
					readPackets();
				}				
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
		if(!hasClients()) {
			return;
		}
		
		IPacketHeader packet = decoder.decode(packetData);
		deliverPacket(packet, header);
	}
	
	
	private void readPackets() {
		if(!pcap.read()) {
			getLogger().error("Read failed on interface ("
					+ getInterface().getName() + ") : " + pcap.getLastError());
			getLogger().warning("Closing interface (" + getInterface().getName()
					+ ") on error.");
			pcap.close();
			pcap = null;
			Thread.currentThread().interrupt();
		}
	}

	@Override
	protected boolean start() {
		if(isStarted()) {
			return true;
		}
		
		final ISniffingEngineService e = getSniffingEngine();
		pcap = iface.pcapCreate(e.getSnaplen(), e.getPromiscuous(), e.getTimeout(), handler);
		
		if(!pcap.open()) {
			getLogger().error("failed to open interface '" 
					+ getInterface().getName() + "' for sniffing: "
					+ pcap.getLastError());
			pcap.close();
			pcap = null;
			return false;
		}
		
		decoder = pcap.getDecoder();
		if(decoder == null) {
			getLogger().error("No decoder available for link type " + pcap.getLinkType() 
					+ " on interface " + getInterface().getName());
			pcap.close();
			pcap = null;
			return false;
		}
		
		sniffingThread = new Thread(sniffingLoop, "Sniffing Engine Thread on Interface (" +
				getInterface().getName() + ")");
		sniffingThread.start();
		return true;
	}

	@Override
	protected void stop() {
		sniffingThread.interrupt();
		if(pcap != null) {
			pcap.close();
		}
		try {
			sniffingThread.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		sniffingThread = null;
		pcap = null;		
	}

}
