package com.netifera.platform.host.terminal.linux;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;
import com.netifera.platform.host.terminal.ITerminal;
import com.netifera.platform.host.terminal.ITerminalOutputHandler;

public class PseudoTerminal implements ITerminal {
	
	/*
	 *  // <include/asm-i386/termios.h>

       0x00005401   TCGETS            struct termios *
       0x00005404   TCSETSF           const struct termios *
       0x00005414   TIOCSWINSZ        const struct winsize *

	 */
	private final static int O_RDWR = 2;
	private final static int O_NOCTTY	=	00000400;
	private final static int TCGETS	=	0x5401;
	private final static int TIOCSWINSZ = 0x5414;
	private final static int TIOCGPTN = 0x80045430;
	private final static int TIOCSPTLCK = 0x40045431;


	private final ISystemService system;
	private final ILogger logger;
	private int master;
	private int slave;
	private int pid;
	private String ptyName;
	private volatile boolean isOpen;
	
	private final String commandName;
	private final ITerminalOutputHandler outputHandler;
	private final Thread readThread;
	
	public PseudoTerminal(String commandName, ITerminalOutputHandler outputHandler, ILogger logger, ISystemService system) {
		this.commandName = commandName;
		this.outputHandler = outputHandler;
		this.logger = logger;
		this.system = system;
		
		this.readThread = new Thread(new Runnable() {
			public void run() {
				readLoop();				
			}
		});
	}
	
	public boolean open() {
		if(!openpty()) {
			return false;
		}
		String[] args = new String[] { "hello" };
		String[] env = new String[] { 
				"PATH=/bin:/usr/bin:/usr/local/bin:/sbin:/usr/sbin",
				"HISTFILE=/dev/null",
				"TERM=xterm-color",
				"HOME=/tmp",
				"PS1=[\\u@\\h \\W]\\$",
				"EDITOR=vi"
				
		};
		pid = system.system_forkexec(commandName, args, env, slave, master);
		
		if(pid < 0) {
			logger.error("fork() and exec() of " + commandName + " failed " + system.getErrorMessage(system.getErrno()));
			system.syscall_close(slave);
			return false;
		}
		
		logger.info("Command launched with child pid = " + pid);
		
		if(system.syscall_close(slave) < 0) {
			logger.warning("Error closing slave file descriptor " + system.getErrorMessage(system.getErrno()));
		}
		isOpen = true;
		readThread.start();
		return true;
		
		
	}
	
	public String getName() {
		return ptyName;
	}
	
	public void sendInput(byte[] data) {
		int off = 0;
		int length = data.length;
		int n;
		while(length > 0) {
			n = system.syscall_write(master, data, off, length);
			if(n < 0) {
				logger.error("write on pty master failed " + system.getErrorMessage(system.getErrno()));
				return;
			}
			off += n;
			length -= n;
		}
	}
	
	boolean openpty() {
		master = system.syscall_open("/dev/ptmx", O_RDWR);
	
		if(master == -1) {
			logger.error("Failed to open pty master : " + system.getErrorMessage(system.getErrno()));
			return false;
		}
		
		ptyName = ptsName(master);
		if(ptyName == null) {
			system.syscall_close(master);
			return false;
		}
		
		if(!unlockpt(master)) 
			return false;
		
		slave = system.syscall_open(ptyName, O_RDWR|O_NOCTTY);
		if(slave < 0) {
			logger.error("Failed to open pty slave at " + ptyName + " : " + system.getErrorMessage(system.getErrno()));
			return false;
		}
		return true;
	}
	
	String ptsName(int fd) {
		
		Termios termios = new Termios();
		if(system.syscall_ioctl(fd, TCGETS, termios.getRawData(), 0, Termios.TERMIOS_SIZE) < 0) {
			logger.warning("TCGETS failed " + system.getErrorMessage(system.getErrno()));
		}
		byte[] buffer = new byte[4];
		if(system.syscall_ioctl(fd, TIOCGPTN, buffer, 4, 4) != 0) {
			logger.error("Failed TIOCGPTN ioctl to determine pty number : " + system.getErrorMessage(system.getErrno()));
			return null;
		}
		int ptyNo = system.unpack32(buffer, 0);
		return "/dev/pts/" + ptyNo;
	}
	
	boolean unlockpt(int fd) {
		byte[] buffer = new byte[4];
		if(system.syscall_ioctl(fd, TIOCSPTLCK, buffer, 4, 0) != 0) {
			logger.error("Failed to unlock pty master : " + system.getErrorMessage(system.getErrno()));
			return false;
		}
		return true;
	}
	
	void readLoop() {
		byte[] readBuffer = new byte[1024];
		int n;
		while(isOpen) {
			n = system.syscall_read(master, readBuffer, 0, readBuffer.length);
			if(n <= 0) {
				if(!isOpen)
					return;
				logger.error("Read failed " + system.getErrorMessage(system.getErrno()));
				if(system.syscall_close(master) < 0) {
					logger.warning("Error closing PTY master " + system.getErrorMessage(system.getErrno()));
				}
				isOpen = false;
				return;
			}
			outputHandler.terminalOutput(ptyName, readBuffer, n);
		}
	}

	public void setSize(int width, int height) {
		if(!isOpen)
			return;
		byte[] data = new byte[8];
		system.pack16(data, 0, height);
		system.pack16(data, 2, width);
		if(system.syscall_ioctl(master, TIOCSWINSZ, data, 8, 0) < 0) {
			logger.warning("Failed to set terminal window size with TIOCSWINSZ " + system.getErrorMessage(system.getErrno()));
		}
	}
	
	public void close() {
		if(!isOpen)
			return;
		if(system.syscall_close(master) < 0) {
			logger.warning("Error closing PTY master " + system.getErrorMessage(system.getErrno()));
		}
	}
}
