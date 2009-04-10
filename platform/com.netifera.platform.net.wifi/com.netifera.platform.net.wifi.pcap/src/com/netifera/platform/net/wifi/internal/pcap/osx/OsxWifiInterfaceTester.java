package com.netifera.platform.net.wifi.internal.pcap.osx;

import java.nio.ByteBuffer;

import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.ICaptureHeader;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.pcap.IPacketCaptureFactoryService;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.wifi.internal.pcap.IWifiInterfaceTester;

public class OsxWifiInterfaceTester implements IWifiInterfaceTester{
	private final IPacketCaptureFactoryService pcapFactory;

	public OsxWifiInterfaceTester(IPacketCaptureFactoryService pcapFactory) {
		this.pcapFactory = pcapFactory;
	}
	private final static IPacketHandler nullHandler = new IPacketHandler() {
		public void handlePacket(ByteBuffer packetData,	ICaptureHeader header) {}
	};
	
	public boolean isWifiDevice(ICaptureInterface iface) {
		

		/* Some arbitrary argument values because we just want to temporarily
		 * open the packet capture device so we can query which types
		 * of datalink are supported.
		 */
		final IPacketCapture pcap = pcapFactory.create(iface, 1000, false, 1000, nullHandler);

		if(!pcap.open()) {
			return false;
		}
		
		for(Datalink dlt : pcap.getDltList()) {
			if(isMonitorDatalink(dlt)) {
				pcap.close();
				return true;
			}
		}
		
		pcap.close();
		return false;
	}
	
	private boolean isMonitorDatalink(Datalink dlt) {
		switch(dlt) {
		case DLT_IEEE802_11:
		case DLT_IEEE802_11_RADIO:
		case DLT_IEEE802_11_RADIO_AVS:
			return true;
			
		default:
			return false;
		}
		
	}


}
