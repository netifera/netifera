package com.netifera.platform.net.sniffing.util;

import com.netifera.platform.net.internal.sniffing.file.CaptureFileException;
import com.netifera.platform.net.internal.sniffing.file.ICaptureFileRecord;
import com.netifera.platform.net.internal.sniffing.file.IPcapCaptureFile;
import com.netifera.platform.net.internal.sniffing.file.PcapCaptureFile;
import com.netifera.platform.net.internal.sniffing.managers.PacketContext;
import com.netifera.platform.net.packets.IPacketDecoder;
import com.netifera.platform.net.packets.IPacketHeader;
import com.netifera.platform.net.packets.decoders.EthernetDecoder;
import com.netifera.platform.net.packets.decoders.GenericDecoder;
import com.netifera.platform.net.packets.decoders.NullDecoder;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.IPacketSniffer;

public class CaptureFileInterface implements ICaptureFileInterface {

	private final static IPacketDecoder ethernetDecoder = new EthernetDecoder();
	private final static IPacketDecoder nullDecoder = new NullDecoder();
	
	private final IPcapCaptureFile captureFile;
	private final boolean isValid;
	private final ISniffingEngineEx sniffingEngine;
	private String errorMessage = "";
	private IPacketDecoder decoder;
	private IPacketSniffer<IPacketHeader> sniffer;
	private boolean processing = false;
	private volatile boolean isCancelled = false;
	

	public CaptureFileInterface(String path, ISniffingEngineEx engine) {
		this.captureFile = new PcapCaptureFile(path);
		this.sniffingEngine = engine;	
		isValid = tryOpen();
	}
	
	public String getPath() {
		return captureFile.getPath();
	}
	
	public boolean captureAvailable() {
		return isValid;
	}
	
	private boolean tryOpen() {
		try {
			captureFile.open();
		} catch (CaptureFileException e) {
			errorMessage = e.getMessage();
			return false;
		}
		
		switch(captureFile.getLinkType()) {
		case DLT_EN10MB:
			decoder = ethernetDecoder;
			return true;
			
		case DLT_NULL:
			decoder = nullDecoder;
			return true;
		}
		
		decoder = GenericDecoder.createForDatalink(captureFile.getLinkType().getConstant());
		if(decoder != null) {
			return true;
		} else {
			errorMessage = "No suitable decoder found for link type: " + captureFile.getLinkType();
			return false;
		}
	}
	
	public String getName() {
		return "Pcap Capture [" + captureFile.getPath() + "]";
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void process() {
		process(null);
	}
	
	public void setSniffer(IPacketSniffer<IPacketHeader> sniffer) {
		this.sniffer = sniffer;
	}
	
	public void cancelProcessing() {
		isCancelled = true;
	}
	
	public synchronized void process(final ICaptureFileProgress progress) {
		if(processing) {
			throw new IllegalStateException("Multiple calls to process() are not allowed");
		}
		
		processing = true;
		isCancelled = false;
		
		final Thread processThread = new Thread(new Runnable() {

			public void run() {
				try {
					runProcess(progress);
				} catch (CaptureFileException e) {
					if(progress != null) {
						progress.error(e.getMessage(), e.getCause());
					}
				} finally {
					finish();
				}
			}
		});

		processThread.start();
	}
	
	public void dispose() {
		sniffingEngine.removeCaptureFileInterface(this);
	}
	private void finish() {
		dispose();
	}
	
	private void runProcess(ICaptureFileProgress progress) throws CaptureFileException {
		while(!isCancelled) {
			ICaptureFileRecord record = captureFile.readRecord();
			if(record == null) {
				if(progress != null) {
					progress.finished();
				}
				return;
			}
			
			IPacketHeader packet;
			try {
				packet = decoder.decode(record.getRecordBytes());
			} catch (Exception e) {
				sniffingEngine.getLogger().error("Unexpected exception decoding packet from capture file: " + e.getMessage(), e);
				continue;
			}
			
			if(sniffer == null) {
				throw new CaptureFileException("No sniffer set to deliver packets to");
			}
			
			try {
				sniffer.handlePacket(packet, new PacketContext(record));
			} catch(Exception e) {
				sniffingEngine.getLogger().error("Exception processing capture file: " + e.getMessage(), e);
			}
			
			if(progress != null) {
				if(!progress.updateProgress(captureFile.getProgress(), captureFile.getPacketCount())) {
					progress.finished();
					return;
				}
			}
		}
		if(isCancelled && progress != null) {
			progress.finished();
		}
	}
}
