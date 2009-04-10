package com.netifera.platform.util.addresses;

// TODO move to com.netifera.platform.util.constants ?

public enum NetworkFamily {
	AF_UNSPEC("Unspecified"),
	AF_INET("IPv4"),
	AF_X25("X.25"),
	AF_IPX("IPX"),
	AF_APPLETALK("AppleTalk"),
	AF_INET6("IPv6"),
	AF_DECNET("DECnet"),
	AF_IRDA("IrDA"),
	AF_BLUETOOTH("Bluetooth");
	
	private String name;
	private NetworkFamily(String familyName) {
		this.name = familyName;
	}
	
	public String getName() {
		return name;
	}
}
