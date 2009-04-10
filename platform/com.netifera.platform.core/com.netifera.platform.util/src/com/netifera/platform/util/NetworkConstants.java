package com.netifera.platform.util;

public interface NetworkConstants {
	/* ARP protocol opcodes. */
	public final static int ARPOP_REQUEST = 1; /* ARP request. */
	public final static int ARPOP_REPLY = 2; /* ARP reply. */
	public final static int ARPOP_RREQUEST = 3; /* RARP request. */
	public final static int ARPOP_RREPLY = 4; /* RARP reply. */
	public final static int ARPOP_InREQUEST = 8; /* InARP request. */
	public final static int ARPOP_InREPLY = 9; /* InARP reply. */
	public final static int ARPOP_NAK = 10; /* (ATM)ARP NAK. */

	/* ARP protocol HARDWARE identifiers. */
	public final static int ARPHRD_NETROM = 0; /* From KA9Q: NET/ROM pseudo. */
	public final static int ARPHRD_ETHER = 1; /* Ethernet 10/100Mbps. */
	public final static int ARPHRD_EETHER = 2; /* Experimental Ethernet. */
	public final static int ARPHRD_AX25 = 3; /* AX.25 Level 2. */
	public final static int ARPHRD_PRONET = 4; /* PROnet token ring. */
	public final static int ARPHRD_CHAOS = 5; /* Chaosnet. */
	public final static int ARPHRD_IEEE802 = 6; /* IEEE 802.2 Ethernet/TR/TB. */
	public final static int ARPHRD_ARCNET = 7; /* ARCnet. */
	public final static int ARPHRD_APPLETLK = 8; /* APPLEtalk. */
	public final static int ARPHRD_DLCI = 15; /* Frame Relay DLCI. */
	public final static int ARPHRD_ATM = 19; /* ATM. */
	public final static int ARPHRD_METRICOM = 23; /* Metricom STRIP (new IANA id). */
	public final static int ARPHRD_IEEE1394 = 24; /* IEEE 1394 IPv4 - RFC 2734. */
	public final static int ARPHRD_EUI64 = 27; /* EUI-64. */
	public final static int ARPHRD_INFINIBAND = 32; /* InfiniBand. */

	/* Dummy types for non ARP hardware */
	public final static int ARPHRD_SLIP = 256;
	public final static int ARPHRD_CSLIP = 257;
	public final static int ARPHRD_SLIP6 = 258;
	public final static int ARPHRD_CSLIP6 = 259;
	public final static int ARPHRD_RSRVD = 260; /* Notional KISS type. */
	public final static int ARPHRD_ADAPT = 264;
	public final static int ARPHRD_ROSE = 270;
	public final static int ARPHRD_X25 = 271; /* CCITT X.25. */
	public final static int ARPHRD_HWX25 = 272; /* Boards with X.25 in firmware. */
	public final static int ARPHRD_PPP = 512;
	public final static int ARPHRD_CISCO = 513; /* Cisco HDLC. */
	public final static int ARPHRD_HDLC = ARPHRD_CISCO;
	public final static int ARPHRD_LAPB = 516; /* LAPB. */
	public final static int ARPHRD_DDCMP = 517; /* Digital's DDCMP. */
	public final static int ARPHRD_RAWHDLC = 518; /* Raw HDLC. */

	public final static int ARPHRD_TUNNEL = 768; /* IPIP tunnel. */
	public final static int ARPHRD_TUNNEL6 = 769; /* IPIP6 tunnel. */
	public final static int ARPHRD_FRAD = 770; /* Frame Relay Access Device. */
	public final static int ARPHRD_SKIP = 771; /* SKIP vif. */
	public final static int ARPHRD_LOOPBACK = 772; /* Loopback device. */
	public final static int ARPHRD_LOCALTLK = 773; /* Localtalk device. */
	public final static int ARPHRD_FDDI = 774; /*
												 * Fiber Distributed Data
												 * Interface.
												 */
	public final static int ARPHRD_BIF = 775; /* AP1000 BIF. */
	public final static int ARPHRD_SIT = 776; /* sit0 device - IPv6-in-IPv4. */
	public final static int ARPHRD_IPDDP = 777; /* IP-in-DDP tunnel. */
	public final static int ARPHRD_IPGRE = 778; /* GRE over IP. */
	public final static int ARPHRD_PIMREG = 779; /* PIMSM register interface. */
	public final static int ARPHRD_HIPPI = 780; /*
												 * High Performance Parallel
												 * I'face.
												 */
	public final static int ARPHRD_ASH = 781; /* (Nexus Electronics) Ash. */
	public final static int ARPHRD_ECONET = 782; /* Acorn Econet. */
	public final static int ARPHRD_IRDA = 783; /* Linux-IrDA. */
	public final static int ARPHRD_FCPP = 784; /* Point to point fibrechanel. */
	public final static int ARPHRD_FCAL = 785; /* Fibrechanel arbitrated loop. */
	public final static int ARPHRD_FCPL = 786; /* Fibrechanel public loop. */
	public final static int ARPHRD_FCFABRIC = 787; /* Fibrechanel fabric. */

	public final static int ARPHRD_IEEE802_TR = 800; /* Magic type ident for TR. */
	public final static int ARPHRD_IEEE80211 = 801; /* IEEE 802.11. */
	public final static int ARPHRD_IEEE80211_PRISM = 802; /*
														 * IEEE 802.11 + Prism2
														 * header.
														 */
	public final static int ARPHRD_IEEE80211_RADIOTAP = 803; /*
															 * IEEE 802.11 +
															 * radiotap header.
															 */
	public final static int ARPHRD_LAPD = 8445;

	public static final int ETHER_TYPE = 1;

	public final static int PACKET_HOST = 0; /* To us. */
	public final static int PACKET_BROADCAST = 1; /* To all. */
	public final static int PACKET_MULTICAST = 2; /* To group. */
	public final static int PACKET_OTHERHOST = 3; /* To someone else. */
	public final static int PACKET_OUTGOING = 4; /* Originated by us . */
	public final static int PACKET_LOOPBACK = 5;
	public final static int PACKET_FASTROUTE = 6;

	public final static int PACKET_MR_PROMISC = 1;
	public final static int PACKET_ADD_MEMBERSHIP = 1;

	/*
	 * ETHER TYPES http://www.iana.org/assignments/ethernet-numbers
	 * http://www.cavebear.com/archive/cavebear/Ethernet/type.html
	 */
	public static final int ETHERTYPE_LOOP = 0x0060; /* Loopback Ethernet */
	public static final int ETHERTYPE_PUP = 0x0200; /* Xerox PUP protocol */
	static final int ETHERTYPE_IP = 0x0800; /* Internet IP (IPv4) */
	public static final int ETHERTYPE_IPv4 = ETHERTYPE_IP;
	public static final int ETHERTYPE_CHAOS = 0x0804; /* Chaosnet */
	public static final int ETHERTYPE_X25 = 0x0805; /* CCITT X.25 */
	public static final int ETHERTYPE_ARP = 0x0806; /* Addr. resolution protocol */
	public static final int ETHERTYPE_FRAMERELAY_ARP = 0x0808; /* Frame Relay */
	public static final int ETHERTYPE_RARP = 0x8035; /*
													 * reverse Addr. resolution
													 * protocol
													 */
	public static final int ETHERTYPE_REVARP = ETHERTYPE_RARP;
	public static final int ETHERTYPE_NS = 0x0600;
	public static final int ETHERTYPE_SPRITE = 0x0500;
	public static final int ETHERTYPE_TRAIL = 0x1000; /* Berkeley Trailer */
	public static final int ETHERTYPE_DEC = 0x6000; /* DEC Unassigned */
	public static final int ETHERTYPE_MOPDL = 0x6001;
	public static final int ETHERTYPE_MOPRC = 0x6002;
	public static final int ETHERTYPE_DN = 0x6003;
	public static final int ETHERTYPE_LAT = 0x6004;
	public static final int ETHERTYPE_SCA = 0x6007;
	public static final int ETHERTYPE_FRAMERELAY_RAW = 0x6559; /* Frame Relay */
	public static final int ETHERTYPE_LANBRIDGE = 0x8038;
	public static final int ETHERTYPE_DECDNS = 0x803c;
	public static final int ETHERTYPE_DECDTS = 0x803e;
	public static final int ETHERTYPE_VEXP = 0x805b;
	public static final int ETHERTYPE_VPROD = 0x805c;
	public static final int ETHERTYPE_ATALK = 0x809b;
	public static final int ETHERTYPE_AARP = 0x80f3;
	public static final int ETHERTYPE_IBMSNA = 0x80d5;
	public static final int ETHERTYPE_8021Q = 0x8100;
	public static final int ETHERTYPE_IPX = 0x8137;
	static final int ETHERTYPE_IPV6 = 0x86dd; /* Internet IPv6 */
	public static final int ETHERTYPE_IPv6 = ETHERTYPE_IPV6;
	public static final int ETHERTYPE_PPP = 0x880b;
	public static final int ETHERTYPE_MPLS = 0x8847; /* MPLS Unicast */
	public static final int ETHERTYPE_MPLS_MULTI = 0x8848; /* MPLS Multicast */
	public static final int ETHERTYPE_PPPOED = 0x8863; /* PPPoE Discovery Stage */
	public static final int ETHERTYPE_PPPOES = 0x8864; /* PPPoE Session Stage */
	public static final int ETHERTYPE_LOOPBACK = 0x9000;

	
	public static final int IPPROTO_IP = 0;
	public static final int IPPROTO_ICMP = 1;
	public static final int IPPROTO_IGMP = 2;
	public static final int IPPROTO_IPIP = 4; /* IPIP tunnels */
	public static final int IPPROTO_TCP = 6;
	public static final int IPPROTO_UDP = 17;
	public static final int IPPROTO_IPV6 = 41; /* IPv6-in-IPv4 tunnelling */
	public static final int IPPROTO_IPv6Fragment = 44;
	public static final int IPPROTO_GRE = 47; /* Cisco GRE tunnels */
	public static final int IPPROTO_ICMPV6 = 58;
	public static final int IPPROTO_RAW = 255;
	
	
	public static final int LINUX_SLL_HOST = 0;
	public static final int LINUX_SLL_BROADCAST = 1;
	public static final int LINUX_SLL_MULTICAST = 2;
	public static final int LINUX_SLL_OTHERHOST = 3;
	public static final int LINUX_SLL_OUTGOING = 4;

	public static final int LINUX_SLL_P_802_3 = 0x0001; /*
														 * Novell 802.3 frames
														 * without 802.2 LLC
														 * header
														 */
	public static final int LINUX_SLL_P_802_2 = 0x0004; /*
														 * 802.2 frames (not
														 * D/I/X Ethernet)
														 */

	public static final int PPP_ADDRESS = 0xff; /* The address byte value */
	public static final int PPP_CONTROL = 0x03; /* The control byte value */

	public static final int PPP_PPPD_IN = 0x00; /* non-standard for DLT_PPP_PPPD */
	public static final int PPP_PPPD_OUT = 0x01; /* non-standard for DLT_PPP_PPPD */

	/* Protocol numbers */
	public static final int PPP_IP = 0x0021; /* Raw IP */
	public static final int PPP_OSI = 0x0023; /* OSI Network Layer */
	public static final int PPP_NS = 0x0025; /* Xerox NS IDP */
	public static final int PPP_DECNET = 0x0027; /* DECnet Phase IV */
	public static final int PPP_APPLE = 0x0029; /* Appletalk */
	public static final int PPP_IPX = 0x002b; /* Novell IPX */
	public static final int PPP_VJC = 0x002d; /* Van Jacobson Compressed TCP/IP */
	public static final int PPP_VJNC = 0x002f; /*
												 * Van Jacobson Uncompressed
												 * TCP/IP
												 */
	public static final int PPP_BRPDU = 0x0031; /* Bridging PDU */
	public static final int PPP_STII = 0x0033; /* Stream Protocol (ST-II) */
	public static final int PPP_VINES = 0x0035; /* Banyan Vines */
	public static final int PPP_IPV6 = 0x0057; /* Internet Protocol version 6 */

	public static final int PPP_HELLO = 0x0201; /* 802.1d Hello Packets */
	public static final int PPP_LUXCOM = 0x0231; /* Luxcom */
	public static final int PPP_SNS = 0x0233; /* Sigma Network Systems */
	public static final int PPP_MPLS_UCAST = 0x0281; /* rfc 3032 */
	public static final int PPP_MPLS_MCAST = 0x0283; /* rfc 3022 */

	public static final int PPP_IPCP = 0x8021; /* IP Control Protocol */
	public static final int PPP_OSICP = 0x8023; /*
												 * OSI Network Layer Control
												 * Protocol
												 */
	public static final int PPP_NSCP = 0x8025; /* Xerox NS IDP Control Protocol */
	public static final int PPP_DECNETCP = 0x8027; /* DECnet Control Protocol */
	public static final int PPP_APPLECP = 0x8029; /* Appletalk Control Protocol */
	public static final int PPP_IPXCP = 0x802b; /* Novell IPX Control Protocol */
	public static final int PPP_STIICP = 0x8033; /*
												 * Strean Protocol Control
												 * Protocol
												 */
	public static final int PPP_VINESCP = 0x8035; /* Banyan Vines Control Protocol */
	public static final int PPP_IPV6CP = 0x8057; /* IPv6 Control Protocol */
	public static final int PPP_MPLSCP = 0x8281; /* rfc 3022 */

	public static final int PPP_LCP = 0xc021; /* Link Control Protocol */
	public static final int PPP_PAP = 0xc023; /* Password Authentication Protocol */
	public static final int PPP_LQM = 0xc025; /* Link Quality Monitoring */
	public static final int PPP_CHAP = 0xc223; /*
												 * Challenge Handshake
												 * Authentication Protocol
												 */
	
	

}
