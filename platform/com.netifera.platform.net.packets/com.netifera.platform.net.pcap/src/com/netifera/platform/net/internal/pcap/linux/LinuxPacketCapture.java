package com.netifera.platform.net.internal.pcap.linux;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.internal.pcap.INativePacketCapture;
import com.netifera.platform.net.internal.pcap.IPacketCaptureInternal;
import com.netifera.platform.net.internal.pcap.PacketHeader;
import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IPacketHandler;
import com.netifera.platform.net.pcap.IPacketCapture.PcapDirection;

public class LinuxPacketCapture implements INativePacketCapture {
	

	private final LinuxPcapOpen openHelper;
	private final ISystemService system;
	private final IPacketCaptureInternal pcap;
	//private final ILogger logger;
	private int socket = -1;
	private int loopbackIndex;
	private int interfaceIndex;
	private boolean cooked;
	private Datalink linktype;
	private int offset;
	private int snaplen;
	private int timeout;
	private ByteBuffer packetBuffer;
	private byte[] packetBufferArray;
	//private byte[] buffer;
	private PcapDirection direction;
	public LinuxPacketCapture(ISystemService system, IPacketCaptureInternal pcap, ILogger logger) {
		this.system = system;
		this.pcap = pcap;
		//this.logger = logger;
		openHelper = new LinuxPcapOpen(this, system, logger);
	}

	public synchronized void close() {
		if(socket == -1) {
			return;
		}	
		system.syscall_close(socket);
		socket = -1;
	}

	private synchronized boolean isClosed() {
		return socket == -1;
	}
	
	public boolean openLive(String device, int snaplen, int timeout, boolean promiscuous) {
	
		if(!openHelper.socketOpenLive(device, promiscuous)) {
			return false;
		}
		
		direction = PcapDirection.PCAP_D_INOUT;
		
		/* We can safely pass "recvfrom()" a byte count
		 * based on the snapshot length.
		 *
		 * If we're in cooked mode, make the snapshot length
		 * large enough to hold a "cooked mode" header plus
		 * 1 byte of packet data (so we don't pass a byte
		 * count of 0 to "recvfrom()").
		 */

		if(cooked && (snaplen < Constants.SLL_HDR_LEN)) {
			snaplen = Constants.SLL_HDR_LEN + 1;
		}
		
		packetBuffer = ByteBuffer.allocateDirect(snaplen + offset);
		if(!packetBuffer.hasArray()) {
			packetBuffer = ByteBuffer.allocate(snaplen + offset);
			if(!packetBuffer.hasArray()) {
				fail("Cannot allocate an array backed ByteBuffer");
				return false;
			}
		}
		packetBuffer.order(ByteOrder.nativeOrder());
		packetBufferArray = packetBuffer.array();
		this.snaplen = snaplen;
		this.timeout = timeout;
		
		return true;
	}

	public Datalink getLinkType() {
		return linktype;
	}
	
	void setLinkType(Datalink dlt) {
		this.linktype = dlt;
	}
	
	int getInterfaceIndex() {
		return interfaceIndex;
	}
	void setInterfaceIndex(int index) {
		interfaceIndex = index;
	}
	
	void setLoopbackIndex(int index) {
		loopbackIndex = index;
	}
	
	void setCooked(boolean cooked) {
		this.cooked = cooked;
	}
	
	int getSocket() {
		return socket;
	}
	
	void setSocket(int s) {
		socket = s;
	}
	
	void dltListClear() {
		pcap.dltListClear();
	}
	
	void dltListAdd(Datalink dlt) {
		pcap.dltListAdd(dlt);
	}
	
	void setOffset(int offset) {
		this.offset = offset;
	}
	

	
	public boolean packetRead(IPacketHandler handler) {
		int readOffset = cooked ? Constants.SLL_HDR_LEN : 0;

		byte[] fds = new byte[8];
		system.pack32(fds, 0, socket);
		system.pack16(fds, 4, Constants.POLLIN);
		system.pack16(fds, 6, 0);
		final int ret = system.syscall_poll(fds, timeout);
		if(ret < 0) {
			if(isClosed())
				return true;
			fail("poll() failed while reading a packet", system.getErrno());
			return false;
			
		}
		final int revents = system.unpack16(fds, 6);
		
		if(ret == 0 ||(revents & Constants.POLLIN) == 0) {
			return true;
		}
		
		byte[] sockaddr_ll = new byte[20];

		int len = system.syscall_recvfrom(socket, packetBufferArray, offset + readOffset, 
				snaplen - readOffset, Constants.MSG_TRUNC, sockaddr_ll, 20);
		
		if(len < 0) {
			if(isClosed()) 
				return true;
			fail("Error reading from packet socket", system.getErrno());
			return false;
		}
		
		packetBuffer.position(offset);
		
		int sll_ifindex = system.unpack32(sockaddr_ll, 4);
		int sll_pkttype = (sockaddr_ll[10] & 0xFF);

		/*
		 * Unfortunately, there is a window between socket() and
		 * bind() where the kernel may queue packets from any
		 * interface.  If we're bound to a particular interface,
		 * discard packets not from that interface.
		 *
		 * (If socket filters are supported, we could do the
		 * same thing we do when changing the filter; however,
		 * that won't handle packet sockets without socket
		 * filter support, and it's a bit more complicated.
		 * It would save some instructions per packet, however.)
		 */
		if(sll_ifindex != interfaceIndex) {
			return true;
		}

		if(sll_pkttype == Constants.PACKET_OUTGOING) {
			/*
			 * Outgoing packet.
			 * If this is from the loopback device, reject it;
			 * we'll see the packet as an incoming packet as well,
			 * and we don't want to see it twice.
			 */
			if(sll_ifindex == loopbackIndex) {
				return true;
			}	
			/*
			 * If the user only wants incoming packets, reject it.
			 */
			if(direction == PcapDirection.PCAP_D_IN) {
				return true;
			}
		} else {
			/*
			 * Incoming packet.
			 * If the user only wants outgoing packets, reject it.
			 */
			if(direction == PcapDirection.PCAP_D_OUT) {
				return true;
			}
		}
		
		if(cooked) {
			len += Constants.SLL_HDR_LEN;
			/*
			 * Map the PACKET_ value to a LINUX_SLL_ value; we
			 * want the same numerical value to be used in
			 * the link-layer header even if the numerical values
			 * for the PACKET_ #defines change, so that programs
			 * that look at the packet type field will always be
			 * able to handle DLT_LINUX_SLL captures.
			 */
			final int packetType;
			switch(sll_pkttype) {
			case Constants.PACKET_HOST:
				packetType = Constants.LINUX_SLL_HOST;
				break;
			case Constants.PACKET_BROADCAST:
				packetType = Constants.LINUX_SLL_BROADCAST;
				break;
			case Constants.PACKET_MULTICAST:
				packetType = Constants.LINUX_SLL_MULTICAST;
				break;
			case Constants.PACKET_OTHERHOST:
				packetType = Constants.LINUX_SLL_OTHERHOST;
				break;
			case Constants.PACKET_OUTGOING:
				packetType = Constants.LINUX_SLL_OUTGOING;
				break;
			default:
				packetType = -1;
			}
			
					
			final int sll_hatype = system.unpack16(sockaddr_ll, 8);
			final int sll_halen = (sockaddr_ll[11] & 0xFF);
			final int sll_protocol = system.unpack16(sockaddr_ll, 2);
			packetBuffer.mark();
			packetBuffer.putShort((short) system.htons(packetType));
			packetBuffer.putShort((short) system.htons(sll_hatype));
			packetBuffer.putShort((short) system.htons(sll_halen));
			packetBuffer.put(sockaddr_ll, 12, 8);
			packetBuffer.putShort((short) sll_protocol);
			packetBuffer.reset();
		}

		int caplen = len;
		if(caplen > snaplen) {
			caplen = snaplen;
		}
		
		/* XXX run userland filter here if needed */
		
		byte[] timeval = new byte[8];
		
		if(system.syscall_ioctl(socket, Constants.SIOCGSTAMP, timeval, 0, 8) == -1) {
			fail("SIOCGSTAMP failed on packet socket", system.getErrno());
			return false;
		}
		
		int seconds = system.unpack32(timeval, 0);
		int useconds = system.unpack32(timeval, 4);
		
		handler.handlePacket(packetBuffer.slice(), new PacketHeader(seconds, useconds, caplen, len));

		return true;
	}
	
	void fail(String message) {
		fail(message, 0);
	}
	
	void fail(String message, int errno) {
		if(errno != 0) {
			pcap.setError(message + " : " + system.getErrorMessage(errno));
		} else {
			pcap.setError(message);
		}
		
		if(!isClosed()) 
			close();
		
		dltListClear();
		
	}

	public int getFileDescriptor() {
		return socket;
	}

	public boolean setDataLink(Datalink dlt) {
		pcap.setError("Setting datalink not supported on linux");
		return false;
	}

}

