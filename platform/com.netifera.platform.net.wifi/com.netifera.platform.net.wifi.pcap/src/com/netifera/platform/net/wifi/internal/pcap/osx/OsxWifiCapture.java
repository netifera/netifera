package com.netifera.platform.net.wifi.internal.pcap.osx;


import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IPacketCapture;
import com.netifera.platform.net.wifi.internal.pcap.IWifiNative;

public class OsxWifiCapture implements IWifiNative {

	@SuppressWarnings("unused")
	private final ISystemService system;
	private final IPacketCapture pcap;
	
	public OsxWifiCapture(ISystemService system, IPacketCapture pcap) {
		this.system = system;
		this.pcap = pcap;
	}

	public boolean enableMonitorMode(boolean enable) {
		boolean found = false;
		for(Datalink dlt : pcap.getDltList()) {
			switch(dlt) {
			case DLT_IEEE802_11:
			case DLT_IEEE802_11_RADIO:
			case DLT_IEEE802_11_RADIO_AVS:
				found = true;
			}
		}
		
		if(!found) {
			pcap.setError("Wireless datalink not found");
			return false;
		}

		// From kismet:
		// 
		// OSX hack which should work on other platforms still, cascade through
		// desired DLTs and the "best one" should stick.  We try in the order we
		// least want - 80211, avs, then radiotap. 

		pcap.setDataLink(Datalink.DLT_IEEE802_11);
		// XXX decoder not implemented yet
		//pcap.setDataLink(Datalink.DLT_IEEE802_11_RADIO_AVS);
		pcap.setDataLink(Datalink.DLT_IEEE802_11_RADIO);
		
		// From kismet:  XXX do we need to do this?
		//
		// Hack to re-enable promisc mode since changing the DLT seems to make it
		// drop it on some bsd pcap implementations
		// ioctl(pcap_get_selectable_fd(pd), BIOCPROMISC, NULL);

		return true;
	}

	public boolean setChannel(int channel) {
		pcap.setError("Setting channel not yet supported");
		return false;
	}
	
	

}
