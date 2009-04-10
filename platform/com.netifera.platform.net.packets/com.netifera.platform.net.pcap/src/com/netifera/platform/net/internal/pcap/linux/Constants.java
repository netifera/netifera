package com.netifera.platform.net.internal.pcap.linux;

public class Constants {
	// sll.h
	static final int SLL_HDR_LEN = 16;
	static final int LINUX_SLL_HOST = 0;
	static final int LINUX_SLL_BROADCAST = 1;
	static final int LINUX_SLL_MULTICAST = 2;
	static final int LINUX_SLL_OTHERHOST = 3;
	static final int LINUX_SLL_OUTGOING = 4;
	// socket.h
	static final int MSG_TRUNC = 0x20;

	// netpacket/packet.h
	static final int PACKET_ADD_MEMBERSHIP = 1;
	static final int PACKET_MR_PROMISC = 1;
	static final int PACKET_HOST = 0;
	static final int PACKET_BROADCAST = 1;
	static final int PACKET_MULTICAST = 2;
	static final int PACKET_OTHERHOST = 3;
	static final int PACKET_OUTGOING = 4;


	// ioctls.h
	static final int SIOCGIFHWADDR = 0x8927;
	static final int SIOCGIFINDEX = 0x8933;

	// sockios.h
	static final int SIOCGSTAMP = 0x8906;

	// errno.h
	static final int EPERM = 1;
	static final int ENODEV = 19;
	
	// if_ether.h
	static final int ETH_P_ALL = 0x0003;

	// socket.h
	static final int PF_PACKET = 17;
	static final int SOCK_DGRAM = 2;
	static final int SOCK_RAW = 3;
	static final int SOL_SOCKET = 1;
	static final int SOL_PACKET = 263;
	static final int SO_ERROR = 4;

	// if.h
	static final int IFNAMSIZ = 16;

	/* ARP protocol HARDWARE identifiers. */
	static final int ARPHRD_ETHER = 1; 
	static final int ARPHRD_EETHER = 2; 
	static final int ARPHRD_AX25 = 3;
	static final int ARPHRD_PRONET = 4; 
	static final int ARPHRD_CHAOS = 5; 
	static final int ARPHRD_IEEE802 = 6; 
	static final int ARPHRD_ARCNET = 7; 
	static final int ARPHRD_DLCI = 15; 
	static final int ARPHRD_ATM = 19; 
	static final int ARPHRD_METRICOM = 23; 
	static final int ARPHRD_SLIP = 256;
	static final int ARPHRD_CSLIP = 257;
	static final int ARPHRD_SLIP6 = 258;
	static final int ARPHRD_CSLIP6 = 259;
	static final int ARPHRD_ADAPT = 264;
	static final int ARPHRD_PPP = 512;
	static final int ARPHRD_CISCO = 513; 
	static final int ARPHRD_RAWHDLC = 518; 
	static final int ARPHRD_TUNNEL = 768; 
	static final int ARPHRD_TUNNEL6 = 769; 
	static final int ARPHRD_FRAD = 770; 
	static final int ARPHRD_LOOPBACK = 772; 
	static final int ARPHRD_LOCALTLK = 773; 
	static final int ARPHRD_FDDI = 774; 
	static final int ARPHRD_SIT = 776; 
	static final int ARPHRD_IPGRE = 778; 
	static final int ARPHRD_IRDA = 783; 
	static final int ARPHRD_FCPP = 784; 
	static final int ARPHRD_FCAL = 785; 
	static final int ARPHRD_FCPL = 786; 
	static final int ARPHRD_FCFABRIC = 787; 
	static final int ARPHRD_IEEE802_TR = 800; 
	static final int ARPHRD_IEEE80211 = 801; 
	static final int ARPHRD_IEEE80211_PRISM = 802; 
	static final int ARPHRD_IEEE80211_RADIOTAP = 803; 
	static final int ARPHRD_LAPD = 8445;
	static final int ARPHRD_NONE = 0xFFFE;
	static final int ARPHRD_VOID = 0xFFFF;
	
	// poll.h
	static final int POLLIN		= 1;

}
