package com.netifera.platform.net.internal.pcap.osx;

public class Constants {
	/* <sys/fcntl.h> */

	static final int O_RDWR = 0x0002;
	static final int O_RDONLY = 0x0000;

	/* <sys/errno.h> */

	static final int EPERM = 1;
	static final int EACCES = 13;
	static final int EBUSY = 16;
	static final int EINTR = 4;
	static final int ENOBUFS = 55;

	/* <net/bpf.h> */

	static final int BIOCVERSION = 0x40044271;
	static final int BIOCGBLEN = 0x40044266;
	static final int BIOCSBLEN = 0xc0044266;
	static final int BIOCSETIF = 0x8020426c;
	static final int BIOCGDLT = 0x4004426a;
	static final int BIOCSDLT = 0x80044278;

	static final int BIOCSHDRCMPLT = 0x80044275;
	static final int BIOCGDLTLIST = 0xc00c4279;
	static final int BIOCSRTIMEOUT = 0x8008426d;
	static final int BIOCPROMISC = 0x20004269;
	static final int BIOCSETF = 0x80084267;
	
	static final int BPF_MAJOR_VERSION = 1;
	static final int BPF_MINOR_VERSION = 1;

	/* <net/if.h> */

	static final int IF_NAMESIZE = 16;

}
