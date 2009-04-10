package com.netifera.platform.host.terminal.linux;

public class Termios {
	// typedef unsigned char   cc_t;
	// typedef unsigned int    speed_t;
	// typedef unsigned int    tcflag_t;
	//
	// #define NCCS 19
	// struct termios {
	//   tcflag_t c_iflag;               /* input mode flags */
    //   tcflag_t c_oflag;               /* output mode flags */
    //   tcflag_t c_cflag;               /* control mode flags */
    //   tcflag_t c_lflag;               /* local mode flags */
    //   cc_t c_line;                    /* line discipline */
    //   cc_t c_cc[NCCS];                /* control characters */
	// };
	public final static int TERMIOS_SIZE = 36;
	
	private final byte[] termiosData = new byte[TERMIOS_SIZE];
	
	byte[] getRawData() {
		return termiosData;
	}
	 

}
