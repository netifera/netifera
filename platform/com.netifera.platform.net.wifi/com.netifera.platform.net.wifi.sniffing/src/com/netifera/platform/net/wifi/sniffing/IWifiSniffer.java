package com.netifera.platform.net.wifi.sniffing;

import com.netifera.platform.net.daemon.sniffing.IPacketModuleContext;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.wifi.packets.WiFiFrame;

public interface IWifiSniffer extends ISniffingModule {
	void handleWifiFrame(WiFiFrame wifi, IPacketModuleContext ctx);


}
