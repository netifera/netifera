package com.netifera.platform.net.internal.pcap.linux;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.pcap.Datalink;

public class LinuxPcapOpen {
	
	private final static int BACKDOOR_COOKED_SOCKET			= 0;
	private final static int BACKDOOR_RAW_SOCKET			= 1;

	private final LinuxPacketCapture pcap;
	private final ISystemService system;
	private final ILogger logger;
	private final LinuxArpMap arpMap;

	
	LinuxPcapOpen(LinuxPacketCapture pcap, ISystemService system, ILogger logger) {
		this.pcap = pcap;
		this.system = system;
		this.logger = logger;
		arpMap = new LinuxArpMap(pcap);
	}
	
	
	boolean socketOpenLive(String device, boolean promiscuous) {

		if(device == null || device.length() == 0) {
			pcap.fail("No device specified");
			return false;
		}
		
		if(!openRaw(device)) {
			return false;
		}
		
		int idx = getInterfaceIndex("lo");
		pcap.setLoopbackIndex(idx);
		
		idx = getInterfaceIndex(device);
		if(idx == -1) {
			pcap.fail("Interface index lookup failed");
			return false;
		}
		pcap.setInterfaceIndex(idx);
		
		if(!bindInterface()) {
			return false;
		}
		
		if(promiscuous) {
			enablePromiscuous(device);
		}
		
		return true;
	}
	
	/*
	 * Open a PF_PACKET socket, store the file descriptor in the
	 * 'fd' field and set the 'linktype' field to the appropriate 
	 * DLT_XXX constant.  If the linktype cannot be determined, or
	 * if it is a device type that runs only in cooked mode, reopen 
	 * a cooked socket and set the 'linktype' to DLT_LINUX_SLL if
	 * necessary.
	 * 
	 * The Linux 'any' device is not supported.
	 */
	@SuppressWarnings("incomplete-switch")
	private boolean openRaw(String device) {
		
		if(!openSocket(false)) {
			return false;
		}
		
		pcap.setCooked(false);
		pcap.setOffset(0);
		
		/*
		 * What kind of frames do we have to deal with? Fall back
		 * to cooked mode if we have an unknown interface type.
		 */
		int arptype = getInterfaceArpType(device);
		if(arptype == -1) {
			return false;
		}
		
		if(!arpMap.mapToDlt(arptype, true)) {
			pcap.setLinkType(Datalink.DLT_NULL);
		}
		
		/*
		 * Unknown interface type (-1), or a
		 * device we explicitly chose to run
		 * in cooked mode (e.g., PPP devices),
		 * or an ISDN device (whose link-layer
		 * type we can only determine by using
		 * APIs that may be different on different
		 * kernels) - reopen in cooked mode.
		 */
		switch(pcap.getLinkType()) {
		case DLT_NULL:
		case DLT_LINUX_SLL:
		case DLT_LINUX_IRDA:
		case DLT_LINUX_LAPD:
		// TODO DLT_EN10MB and 	isdn || isdY
			return openCooked(device);
		}
		
		
		return true;		
	}

	/* Called from openRaw() to fallback to a cooked socket */
	private boolean openCooked(String device) {
		if(!openSocket(true)) {
			return false;
		}
		pcap.setCooked(true);
		pcap.dltListClear();
		if(pcap.getLinkType() != Datalink.DLT_LINUX_IRDA && pcap.getLinkType() != Datalink.DLT_LINUX_LAPD) {
			pcap.setLinkType(Datalink.DLT_LINUX_SLL);
		}
		return true;
	}
	
	/*
	 * Open either a cooked or raw socket depending on the
	 * value of the 'cooked' argument.
	 */
	private boolean openSocket(boolean cooked) {
		if(pcap.getSocket() != -1) {
			pcap.close();
		}
		
		final int type = cooked ? (Constants.SOCK_DGRAM) : (Constants.SOCK_RAW);
		final int s = system.syscall_socket(Constants.PF_PACKET, type, system.htons(Constants.ETH_P_ALL));
		
		if(s < 0) {
			final int errno = system.getErrno();
			if(errno == Constants.EPERM) {
				return backdoorOpenSocket(cooked);
			}
			pcap.fail("Error opening capture socket", errno);
			return false;
		}
		
		pcap.setSocket(s);
		return true;
	}
	
	private boolean backdoorOpenSocket(boolean cooked) {
		final int request = cooked ? (BACKDOOR_COOKED_SOCKET) : (BACKDOOR_RAW_SOCKET);
		final int s = system.backdoor_request(request);
		if(s < 0) {
			final int errno = system.getErrno();
			if(errno == Constants.EPERM) {
				pcap.fail("Permission error opening socket with backdoor.  (Is backdoor setuid?)");
				return false;
			}

			pcap.fail("Error attempting to open capture socket with backdoor", errno);
			return false;
		}
		pcap.setSocket(s);
		return true;
	}
	private byte[] deviceNameToStruct(String device) {
		if(device.length() >= Constants.IFNAMSIZ) {
			pcap.fail("Device name too long " + device);
			return null;
		}
		
		// struct ifreq

		final byte[] ifreq = new byte[40];

		for(int i = 0; i < device.length(); i++) {
			ifreq[i] = (byte) device.charAt(i);
		}
		
		return ifreq;
		
	}
	
	/*
	 *  Get the hardware type of the given interface as ARPHRD_xxx constant.
	 */
	private int getInterfaceArpType(String device) {
		final byte[] data = deviceNameToStruct(device);
		if(data == null) {
			return -1;
		}
			
		if(system.syscall_ioctl(pcap.getSocket(), Constants.SIOCGIFHWADDR, data, 40, 40) < 0) {
			if(system.getErrno() == Constants.ENODEV) {
				pcap.fail("Could not open device '" + device + "'");
				return -1;
			}
			
			pcap.fail("ioctl SIOCGIFHWADDR failed", system.getErrno());
			return -1;
		}
		
		return system.unpack16(data, Constants.IFNAMSIZ);
	}
	
	
	private int getInterfaceIndex(String device) {
	final byte[] data = deviceNameToStruct(device);
		
		if(system.syscall_ioctl(pcap.getSocket(), Constants.SIOCGIFINDEX, data, 40, 40) < 0) {
			pcap.fail("ioctl SIOCGIFINDEX failed", system.getErrno());
			return -1;
		}
		return system.unpack32(data, Constants.IFNAMSIZ);
	}
	
	private boolean bindInterface() {
		
		final int s = pcap.getSocket();
		final int idx = pcap.getInterfaceIndex();
		if( (s == -1) || (idx == -1)) {
			pcap.fail("Socket or interface invalid");
			return false;
		}
		
		final byte[] sockaddr_ll = new byte[20];
		
		/* sll.sll_family = PF_PACKET */
		system.pack16(sockaddr_ll, 0, Constants.PF_PACKET);
		
		/* sll.sll_ifindex = ifIndex */
		system.pack32(sockaddr_ll, 4, idx);
		
		/* sll.sll_protocol = htons(ETH_P_ALL) */
		system.pack16(sockaddr_ll, 2, system.htons(Constants.ETH_P_ALL));
		
		if(system.syscall_bind(s, sockaddr_ll, 20) < 0) {
			pcap.fail("bind() interface failed", system.getErrno());
			return false;
		}
		
		final byte[] errbuf = new byte[4];
		if(system.syscall_getsockopt(s, Constants.SOL_SOCKET, Constants.SO_ERROR, errbuf, 4) < 0) {
			pcap.fail("getsockopt() failed", system.getErrno());
			return false;
		}
		
		int errno = system.unpack32(errbuf, 0);
		if(errno != 0) {
			pcap.fail("SO_ERROR after bind()", errno);
			return false;
		}
		
		return true;
	}
	
	private void enablePromiscuous(String device) {
		/* sizeof(struct packet_mreq) = 16 */
		byte[] data = new byte[16];
		
		/* packet_mreq.mr_ifindex */
		system.pack32(data, 0, pcap.getInterfaceIndex());
		
		/* packet_mreq.mr_type */
		system.pack16(data, 4, Constants.PACKET_MR_PROMISC);
		
		if(system.syscall_setsockopt(pcap.getSocket(), Constants.SOL_PACKET, Constants.PACKET_ADD_MEMBERSHIP, data, 16) < 0) {
			logger.warning("enablePromiscuous() failed for " + device + ": "+ system.getErrorMessage(system.getErrno()));
			// ignore failure
		}
	}
}
