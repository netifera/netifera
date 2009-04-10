package com.netifera.platform.net.routes;


import com.netifera.platform.util.addresses.inet.InternetAddress;

public interface IIP2ASService {
	AS getAS(InternetAddress address);
}
