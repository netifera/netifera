package com.netifera.platform.net.wifi.pcap;

import java.util.Collection;

import com.netifera.platform.net.pcap.IPacketHandler;

public interface IWifiPacketCaptureFactory {
	/**
	 * Return a collection of all available wireless devices or an
	 * empty set if no wireless network interfaces are found.
	 * 
	 * @return Collection of <code>NetworkInterface</code> instances containing
	 * all wireless network devices available.
	 */
	Collection<IWirelessCaptureInterface> getWifiInterfaces();
	
	/**
	 * Create a new <code>IWifiPacketCapture</code> instance which is bound to
	 * the specified parameters and <code>IPacketHandler</code>.
	 * 
	 * @param iface The wireless network interface this instance is associated with
	 * @param snaplen The capture 'snaplen' which is the maximum number of bytes to capture for each 
	 * packet.
	 * @param promiscuous If true, the device will be opened in 'promiscuous mode' if possible.
	 * @param timeout Read timeout on the device in milliseconds.
	 * @param packetHandler The callback which will be used for processing packets
	 * captured by this <code>IWifiPacketCapture</code> instance.
	 * 
	 * @return The newly created <code>IWifiPacketCapture</code> instance.
	 */
	IWifiPacketCapture create(IWirelessCaptureInterface iface, int snaplen, boolean promiscuous,
			int timeout, IPacketHandler packetHandler);
}
