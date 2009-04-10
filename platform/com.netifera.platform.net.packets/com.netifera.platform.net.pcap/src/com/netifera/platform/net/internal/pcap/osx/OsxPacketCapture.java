package com.netifera.platform.net.internal.pcap.osx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.net.internal.pcap.INativePacketCapture;
import com.netifera.platform.net.internal.pcap.IPacketCaptureInternal;
import com.netifera.platform.net.internal.pcap.PacketHeader;
import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IBPFProgram;
import com.netifera.platform.net.pcap.IPacketHandler;

public class OsxPacketCapture implements INativePacketCapture {


	private final static int MIN_BPF_HEADER_LEN = 18;
	private final static int BACKDOOR_OPEN_RDONLY = 0;
	
	private final ISystemService system;
	private final IPacketCaptureInternal pcap;
	//private final ILogger logger;
	private final int pointerSize;

	private int fd = -1;
	private Datalink linktype = Datalink.DLT_INVALID;
	
	private ByteBuffer packetBuffer; /* packs/unpacks in native order */
	private byte[] packetBufferArray;
	

	

	public OsxPacketCapture(ISystemService system, IPacketCaptureInternal pcap, ILogger logger) {
		if(system.getArch() != ISystemService.SystemArch.ARCH_X86) {
			throw new IllegalStateException("Implementation only supports x86 architecture");
		}
		this.system = system;
		this.pcap = pcap;
		//this.logger = logger;
		
		/* For x86 */
		pointerSize = 4;
	}

	public Datalink getLinkType() {
		return linktype;
	}
	
	public boolean setDataLink(Datalink dlt) {
		byte[] linkbuf = new byte[4];
		system.pack32(linkbuf, 0, dlt.getConstant());
		if(system.syscall_ioctl(fd, Constants.BIOCSDLT, linkbuf, 4, 0) == -1) {
			pcap.setError("BIOCSDLT failed setting link type : " + system.getErrorMessage(system.getErrno()));
			return false;
		}
		
		linktype = queryDlt();
		return true;
	}

	
	/**
	 * Skip over the specified number of bytes in the packetBuffer by incrementing the <tt>position</tt>
	 * pointer if possible.  If there is not enough space left to skip the requested number of bytes, 
	 * return <tt>false</tt> and do not change the position pointer.
	 * 
	 * @param n Number of bytes to skip.
	 * @return <tt>false</tt> if there is not enough space left in buffer to skip the requested number
	 * of bytes.  Returns <tt>true</tt> if the bytes have been skipped.
	 */
	private boolean packetBufferSkipBytes(int n) {
		if(packetBuffer.remaining() < n) {
			return false;
		}
		if(n > 0) {
			packetBuffer.position( packetBuffer.position() + n );
		}
		return true;
	}
	
	/**
	 * Return a ByteBuffer which is a 'slice' of the packetBuffer that contains 
	 * <code>numBytes</code> of data.  The <tt>position</tt> field of the returned
	 * ByteBuffer points to the current <tt>position</tt> of <code>packetBuffer</code>.
	 * and <tt>limit</tt> points to the of the <code>numBytes</code> chunk.  The position 
	 * of <code>packetBuffer</code> is incremented by <code>numBytes</code>.
	 * 
	 * @param numBytes The size of the packetBuffer 'chunk' to return.
	 * @return A new ByteBuffer of the requested size or <tt>null</tt> if the requested 
	 * number of bytes are not available in <code>packetBuffer</code>
	 */
	private ByteBuffer getChunk(int numBytes) {
		if(packetBuffer.remaining() < numBytes) {
			return null;
		}
		final ByteBuffer tmp = packetBuffer.duplicate();
		packetBufferSkipBytes(numBytes);
		tmp.limit( tmp.position() + numBytes );
		return tmp.slice();
	}
	
	/**
	 * Read data from the BPF descriptor into the packet buffer.
	 * 
	 * @return <tt>false</tt> if the <code>syscall_read</code> system call returns an error.
	 */
	private boolean fillBuffer() {
		while(true) {
			int n = system.syscall_read(fd, packetBufferArray, 0, packetBufferArray.length);
			if(n < 0) {
				if(system.getErrno() == Constants.EINTR) {
					continue;
				} else if(isClosed()) { // Cancelled?
					packetBuffer.clear();
					packetBuffer.limit(0);
					return true;
				} else {
					fail("Read failed", system.getErrno());
					return false;
				}
			}
			
			if(n > 0) {
				packetBuffer.clear();
				packetBuffer.limit(n);
				return true;
			}	
		}
	}
	
	/**
	 * Process a single packet by reading data from the current position in the 
	 * <code>packetBuffer</code>, parsing the BPF header, and calling the handler
	 * with the packet data.  Returns <tt>false</tt> if there is not enough data
	 * remaining in <code>packetBuffer</code> to process a packet.
	 * 
	 * @param handler The handler callback to call when a packet has been extracted and processed.
	 * @return <tt>true</tt> if the packet is successfully extracted and delivered.
	 */
	private boolean processPacket(IPacketHandler handler) {
		
		if(packetBuffer.remaining() < MIN_BPF_HEADER_LEN) {
			return false;
		}
		
		// struct bpf_hdr {
		// [0] struct BPF_TIMEVAL bh_tstamp; /* time stamp */
		// [8] bpf_u_int32 bh_caplen; /* length of captured portion */
		// [12] bpf_u_int32 bh_datalen; /* original length of packet */
		// [16] u_short bh_hdrlen; /* length of bpf header (this struct
		// plus alignment padding) */
		// };
		
		final int seconds = packetBuffer.getInt();
		final int useconds = packetBuffer.getInt();
		final int caplen = packetBuffer.getInt();
		final int datalen = packetBuffer.getInt();
		final int hdrlen = packetBuffer.getShort();
		
		
		if(hdrlen > MIN_BPF_HEADER_LEN) {
			int pad = hdrlen - MIN_BPF_HEADER_LEN;
			if(pad > 8) {
				// XXX ridiculous pad length
			}
		
			if(!packetBufferSkipBytes(pad)) {
				return false;
			}
		}
		
		ByteBuffer data = getChunk(caplen);
		if(data == null) {
			return false;
		}
		
		handler.handlePacket(data, new PacketHeader(seconds, useconds, caplen, datalen));
		
		// word align
		if((packetBuffer.position() & 3) != 0) {
			final int alignPos = (packetBuffer.position() + 3) & ~3;
			if(alignPos > packetBuffer.limit()) {
				return false;
			}
			packetBuffer.position(alignPos);
		}
		
		return true;
	
	}
	
	public boolean packetRead(IPacketHandler handler) {
		
		if(!fillBuffer()) {
			return false;
		}

		while(packetBuffer.hasRemaining()) {
			if(processPacket(handler) == false) {
				break;
			}
		}
		
		return true;
	}
	

	

	public boolean openLive(String device, int snaplen, int timeout,
			boolean promiscuous) {

		if (device.length() == 0 || device.length() >= Constants.IF_NAMESIZE) {
			fail("Illegal device name: " + device);
			return false;
		}

		if (!openBPF()) {
			return false;
		}

		if (!checkVersion()) {
			return false;
		}

		if (!setBufferLength(device)) {
			return false;
		}

		if (!setLinkType(device)) {
			return false;
		}

		if (!setHeaderComplete(device)) {
			return false;
		}

		if (!setTimeout(device, timeout)) {
			return false;
		}

		if (promiscuous) {
			if (system.syscall_ioctl(fd, Constants.BIOCPROMISC, new byte[0], 0, 0) < 0) {
				// do nothing if this fails
			}
		}

		if(!getBufferLength(device)) {
			return false;
		}
		
		if (!setDefaultFilter(device, snaplen)) {
			return false;
		}

		return true;
	}

	private boolean openBPF() {

		if (fd != -1) {
			closeBPF();
		}

		for (int i = 0; i < 255; i++) {
			String path = "/dev/bpf" + i;

			fd = system.syscall_open(path, Constants.O_RDWR);

			if (fd < 0 && system.getErrno() == Constants.EACCES) {
				fd = system.syscall_open(path, Constants.O_RDONLY);
			}

			if (fd >= 0) {
				return true;
			}

			if (system.getErrno() != Constants.EBUSY) {
				break;
			}
		}

		if(backdoorOpenBPF()) {
			return true;
		}
		
		fd = -1;
		fail("Failed to open BPF device.", system.getErrno());

		return false;

	}
	
	private boolean backdoorOpenBPF() {
		fd = system.backdoor_request(BACKDOOR_OPEN_RDONLY);
		if(fd < 0) {
			final int errno = system.getErrno();
			if(errno == Constants.EPERM) {
				fail("Permission error opening BPF with backdoor. (Is backdoor setuid?)");
				return false;
			}
			fail("Error attempting to open BPF with backdoor", errno);
			return false;
		}
		return true;
	}
	
	public void close() {
		closeBPF();
	}

	private synchronized void closeBPF() {
		if (fd == -1)
			return;
		system.syscall_close(fd);
		fd = -1;
	}
	
	private synchronized boolean isClosed() {
		return fd == -1;
	}

	private boolean checkVersion() {

		final byte[] data = new byte[4];

		if (system.syscall_ioctl(fd, Constants.BIOCVERSION, data, 0, 4) < 0) {
			fail("ioctl(BIOCVERSION) failed.", system.getErrno());
			return false;
		}

		final int major = system.unpack16(data, 0);
		final int minor = system.unpack16(data, 2);

		if (major != Constants.BPF_MAJOR_VERSION || minor < Constants.BPF_MINOR_VERSION) {
			fail("Kernel BPF filter version mismatch");
			return false;
		}

		return true;
	}

	private boolean setBufferLength(String device) {
		int v = 32768;
		final byte[] data = new byte[4];

		if (system.syscall_ioctl(fd, Constants.BIOCGBLEN, data, 0, 4) >= 0) {
			int n = system.unpack32(data, 0);
			if (n > 32768) {
				v = n;
			}
		}

		/* sizeof(struct ifreq) == 32 on osx */
		final byte[] ifr = new byte[32];
		while (v != 0) {
			system.syscall_ioctl(fd, Constants.BIOCSBLEN, data, 4, 0);

			for (int i = 0; i < device.length(); i++) {
				ifr[i] = (byte) device.charAt(i);
			}
			ifr[device.length()] = 0;

			if (system.syscall_ioctl(fd, Constants.BIOCSETIF, ifr, 32, 0) >= 0) {
				break;
			}
			if (system.getErrno() != Constants.ENOBUFS) {
				fail("BIOCSETIF failed for " + device, system.getErrno());
				return false;
			}
			v >>= 1;
		}

		if (v == 0) {
			fail("BIOCSBLEN for " + device + " failed.  No buffer size worked.");
			return false;
		}

		return true;

	}
	
	private Datalink queryDlt() {
		final byte[] data = new byte[4];
		/* get the link type */
		if (system.syscall_ioctl(fd, Constants.BIOCGDLT, data, 0, 4) < 0) {
			fail("BIOCGDLT failed on " + pcap.getInterfaceName(), system.getErrno());
			return Datalink.DLT_INVALID;
		}
		
		return pcap.dltLookup(system.unpack32(data, 0));
	}

	/*
	 * Synchronization is required because of access to the native internal
	 * buffer needed to implement BIOCGDLTLIST
	 */
	private synchronized boolean setLinkType(String device) {
		
		linktype = queryDlt();
		if(linktype == Datalink.DLT_INVALID) {
			return false;
		}
		
		// struct bpf_dltlist {
		// 		u_int32_t bfl_len; /* number of bfd_list array */
		// 		union {
		// 			u_int32_t *bflu_list; /* array of DLTs */
		// 			u_int64_t bflu_pad;
		// 		} bfl_u;
		// };
		// #define bfl_list bfl_u.bflu_list
		
		final byte[] bpf_dltlist = new byte[12];
  
		if (system.syscall_ioctl(fd, Constants.BIOCGDLTLIST, bpf_dltlist, bpf_dltlist.length, bpf_dltlist.length) != 0) {
				fail("BIOCGDLTLIST failed on " + device, system.getErrno());
				return false;
		}
		
		int bfl_len = system.unpack32(bpf_dltlist, 0);
					
		final byte[] bfl_list = new byte[bfl_len * 4];
		
		if(!packNestedStructure(bfl_list, bfl_list.length, bpf_dltlist, 4)) {
			return false;
		}
				
		if (system.syscall_ioctl(fd, Constants.BIOCGDLTLIST, bpf_dltlist, bpf_dltlist.length,
				bpf_dltlist.length) < 0) {
			fail("Second BIOCGDLTLIST failed on " + device, system.getErrno());
			return false;
		}

		if(system.system_getbuffer(bfl_list, bfl_list.length) == -1) {
			fail("system_getbuffer() failed while opening " + device, system.getErrno());
			return false;
		}
		
		bfl_len = system.unpack32(bpf_dltlist, 0);
		
		for (int i = 0; i < bfl_len; i++) {
			Datalink dlt = pcap.dltLookup(system.unpack32(bfl_list, i * 4));
			if(dlt != Datalink.DLT_INVALID) {
				pcap.dltListAdd(dlt);
			}
		}

		/*
		 * OK, for real Ethernet devices, add DLT_DOCSIS to the list, so
		 * that an application can let you choose it, in case you're
		 * capturing DOCSIS traffic that a Cisco Cable Modem Termination
		 * System is putting out onto an Ethernet (it doesn't put an
		 * Ethernet header onto the wire, it puts raw DOCSIS frames out on
		 * the wire inside the low-level Ethernet framing).
		 * 
		 * A "real Ethernet device" is defined here as a device that has a
		 * link-layer type of DLT_EN10MB and that has no alternate
		 * link-layer types; that's done to exclude 802.11 interfaces (which
		 * might or might not be the right thing to do, but I suspect it is
		 * - Ethernet <-> 802.11 bridges would probably badly mishandle
		 * frames that don't have Ethernet headers).
		 */
		if (linktype == Datalink.DLT_EN10MB) {
			boolean isEthernet = true;
			for(Datalink d : pcap.getDltList()) {
				if (d != Datalink.DLT_EN10MB) {
					isEthernet = false;
					break;
				}
			}
			
			if (isEthernet) {
				pcap.dltListAdd(Datalink.DLT_DOCSIS);
			}		

		}

		/*
		 * If this is an Ethernet device, and we don't have a DLT_ list, give it
		 * a list with DLT_EN10MB and DLT_DOCSIS. (That'd give 802.11 interfaces
		 * DLT_DOCSIS, which isn't the right thing to do, but there's not much
		 * we can do about that without finding some other way of determining
		 * whether it's an Ethernet or 802.11 device.)
		 */

		if (linktype == Datalink.DLT_EN10MB && pcap.getDltList().isEmpty()) {
			pcap.dltListAdd(Datalink.DLT_EN10MB);
			pcap.dltListAdd(Datalink.DLT_DOCSIS);
		}
		return true;
	}

	private boolean setHeaderComplete(String device) {
		/*
		 * Do a BIOCSHDRCMPLT, if defined, to turn that flag on, so the
		 * link-layer source address isn't forcibly overwritten. (Should we
		 * ignore errors? Should we do this only if we're open for writing?)
		 * 
		 * XXX - I seem to remember some packet-sending bug in some BSDs - check
		 * CVS log for "bpf.c"?
		 */

		final byte[] flag = new byte[4];
		system.pack32(flag, 0, 1);
		if (system.syscall_ioctl(fd, Constants.BIOCSHDRCMPLT, flag, 4, 0) < 0) {
			fail("BIOCSHDRCMPLT failed on " + device, system.getErrno());
			return false;
		}

		return true;
	}

	private boolean setTimeout(String device, int timeout) {
		if (timeout != 0) {
			final byte[] tv = new byte[8];
			/* seconds */
			system.pack32(tv, 0, timeout / 1000);
			/* milliseconds */
			system.pack32(tv, 4, (timeout * 1000) % 1000000);
			if (system.syscall_ioctl(fd, Constants.BIOCSRTIMEOUT, tv, 8, 0) < 0) {
				fail("BIOCSRTIMEOUT failed on " + device, system.getErrno());
				return false;
			}

		}
		return true;
	}

	private synchronized boolean setDefaultFilter(String device, int snaplen) {

		/*
		 * If there's no filter program installed, there's no indication to the
		 * kernel of what the snapshot length should be, so no snapshotting is
		 * done.
		 * 
		 * Therefore, when we open the device, we install an "accept everything"
		 * filter with the specified snapshot length.
		 */

		final IBPFProgram bpf = pcap.createBPFProgram();
		bpf.addInstruction(IBPFProgram.BPF_RET | IBPFProgram.BPF_K, 0, 0,
				snaplen);

		final byte[] bpfBytes = bpf.getBytes();
		final byte[] bpf_program = new byte[4 + pointerSize];

		if(!packNestedStructure(bpfBytes, bpfBytes.length, bpf_program, 4)) {
			return false;
		}
		
		system.pack32(bpf_program, 0, bpf.getInstructionCount());
		
		if (system.syscall_ioctl(fd, Constants.BIOCSETF, bpf_program, bpf_program.length, 0) < 0) {
			fail("BIOCSETF failed on " + device, system.getErrno());
			return false;
		}
		
		return true;
	}
	
	
	private boolean getBufferLength(String device) {
		byte[] data = new byte[4];
		
		if(system.syscall_ioctl(fd, Constants.BIOCGBLEN, data, 0, 4) < 0) {
			fail("BIOCGBLEN failed for " + device, system.getErrno());
			return false;
		}
		
		int bufferLength = system.unpack32(data, 0);
		packetBuffer = ByteBuffer.allocateDirect(bufferLength);
		if(!packetBuffer.hasArray()) {
			packetBuffer = ByteBuffer.allocate(bufferLength);
			if(!packetBuffer.hasArray()) {
				fail("Cannot allocate an array backed ByteBuffer");
				return false;
			}
		}
		packetBuffer.order(ByteOrder.nativeOrder());
		packetBufferArray = packetBuffer.array();
		return true;
	}
	
	private boolean packNestedStructure(byte[] data, int length, byte[] structure, int pointerOffset) {
		final byte[] addressBuffer = new byte[pointerSize];
		if(system.system_putbuffer(data, length, addressBuffer) == -1) {
			fail("system_putbuffer() failed.", system.getErrno());
			return false;
		}
		System.arraycopy(addressBuffer, 0, structure, pointerOffset, pointerSize);
		return true;
	}
	
	private void fail(String message) {
		fail(message, 0);
	}
	
	private void fail(String message, int errno) {
		if(errno != 0) {
			pcap.setError(message + " : " + system.getErrorMessage(errno));
		} else {
			pcap.setError(message);
		}
		
		if(fd != -1) {
			closeBPF();
		}
		
	}

	public int getFileDescriptor() {
		return fd;
	}

}
