package com.netifera.platform.util;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import com.netifera.platform.util.addresses.inet.IPv4Address;

@Deprecated // not used: remove from codebase?
public class InterfaceUtility {
	private static final int IPV4_ONLY = 1;
	public static NetworkInterface defaultInterface() {
		return defaultInterface(0);
	}
	public static NetworkInterface defaultInet4Interface() {
		return defaultInterface(IPV4_ONLY);
	}
	
	private static NetworkInterface defaultInterface(int flags) {
		Enumeration<NetworkInterface> ifs;
		try {
			ifs = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		Enumeration<InetAddress> ads;
		
		for(NetworkInterface netif : Collections.list(ifs)) {
			ads = netif.getInetAddresses();
			for(InetAddress addr : Collections.list(ads)) {
				
				if(!addr.isLoopbackAddress()) {
					
					if((flags == IPV4_ONLY) && ((addr instanceof Inet4Address) == false)) {
						continue;
					}
					
					return netif;
				}
			}
		}
		return null;
	}
	
	public static IPv4Address defaultInet4Address() {
		NetworkInterface netif = defaultInet4Interface();
		if(netif == null) {
			return null;
		}
		
		Enumeration<InetAddress> ads = netif.getInetAddresses();
		for(InetAddress  addr : Collections.list(ads)) {
			if(addr instanceof Inet4Address) {		
				return new IPv4Address( addr.getAddress() );
			}
		}
		
		return null;
		
		
	}
		
	public static IPv4Address getIPv4SourceAddressFor(IPv4Address dst) {
		IPv4Address src;
        try {
			// KLUDGE (sean) to lookup ip source address without parsing routing table.
            InetAddress address = InetAddress.getByAddress(dst.toBytes());
			DatagramSocket s = new DatagramSocket(0);
			s.connect(address, 1);
			Inet4Address ia = (Inet4Address)s.getLocalAddress();
			try {
				s.close();
			} catch (Exception e) {
			}
			if (ia == null)
				return null;
			src = new IPv4Address(ia.getAddress());
		} catch (UnknownHostException e) {
			return null;
		} catch (SocketException e) {
			return null;
		}
		return src;
    }
}
