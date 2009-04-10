package com.netifera.platform.net.geoip;

import com.netifera.platform.util.addresses.inet.InternetAddress;

public interface IGeoIPService {
	ILocation getLocation(InternetAddress address);
}
