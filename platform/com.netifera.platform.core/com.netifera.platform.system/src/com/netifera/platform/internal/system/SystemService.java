package com.netifera.platform.internal.system;


import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.system.ISystemService;

public class SystemService implements ISystemService {
	
	
	private final Bytesex	bytesex	= new Bytesex();
	private final ISystemNative sysNative = new SystemNative();
	private final Backdoor backdoor = new Backdoor();
	private ILogger logger;
	private String backdoorPath;
	
	private final ThreadLocal<Integer> errno = new ThreadLocal<Integer>() {
		public Integer initialValue() {
			return 0;
		}
	};
	
	public int backdoor_request(int request) {
		if(backdoorPath == null) {
			return -1;
		}
		return sysNative.native_backdoor(backdoorPath, request);
	}
	
	public String backdoor_path() {
		return backdoorPath;
	}
	
	public int syscall_close(int fd) {
		return syscall(sysNative.native_close(fd));
	}

	public int syscall_ioctl(int fd, int request, byte[] data, int inlen,
			int outlen) {
		return syscall(sysNative.native_ioctl(fd, request, data, inlen, outlen));
	}

	public int syscall_open(String path, int flags) {
		return syscall(sysNative.native_open(path, flags));
	}

	public int syscall_read(int fd, byte[] buffer, int offset, int length) {
		return syscall(sysNative.native_read(fd, buffer, offset, length));
	}

	public int syscall_write(int fd, byte[] buffer, int offset, int length) {
		return syscall(sysNative.native_write(fd, buffer, offset, length));
	}
	
	public int getErrno() {
		return errno.get();
	}
	public String getErrorMessage(int errno) {
		return sysNative.getErrorMessage(errno);
	}
	public void pack16(byte[] data, int offset, int value) {
		bytesex.pack16(data, offset, value);
	}

	public void pack32(byte[] data, int offset, int value) {
		bytesex.pack32(data, offset, value);
	}

	public int unpack16(byte[] data, int offset) {
		return bytesex.unpack16(data, offset);
	}

	public int unpack32(byte[] data, int offset) {
		return bytesex.unpack32(data, offset);
	}

	public int ntohs(int n) {
		return bytesex.ntohs(n);	
	}
	
	public int ntohl(int n) {
		return bytesex.ntohl(n);
	}
	
	public int htons(int n) {
		return bytesex.htons(n);
	}
	
	public int htonl(int n) {
		return bytesex.htonl(n);
	}
	
	public SystemOS getOS() {
		return sysNative.getOS();
	}

	public SystemArch getArch() {
		return sysNative.getArch();
	}
	public int system_putbuffer(byte[] data, int length, byte[] address) {
		return syscall(sysNative.native_putbuffer(data, length, address));
	}
	
	public int system_getbuffer(byte[] data, int length) {
		return syscall(sysNative.native_getbuffer(data, length));
	}
	public int syscall_bind(int fd, byte[] addr, int addrlen) {
		return syscall(sysNative.native_bind(fd, addr, addrlen));
	}
	public int syscall_getsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen) {
		return syscall(sysNative.native_getsockopt(fd, level, optname, optdata, optlen));
	}
	public int syscall_setsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen) {
		return syscall(sysNative.native_setsockopt(fd, level, optname, optdata, optlen));
	}
	public int syscall_socket(int domain, int type, int protocol) {
		return syscall(sysNative.native_socket(domain, type, protocol));
	}
	
	public int syscall_recvfrom(int fd, byte[] buffer, int offset, int length, int flags, byte[] addr, int addrlen) {
		return syscall(sysNative.native_recvfrom(fd, buffer, offset, length, flags, addr, addrlen));
	}

	public int syscall_poll(byte[] fds, int timeout) {
		return syscall(sysNative.native_poll(fds, timeout));
	}

	public int system_forkexec(String path, String argv[], String env[], int fd, int master) {
		return syscall(sysNative.native_forkexec(path, argv, env, fd, master));
	}
	
	private int syscall(int ret) {
		if(ret < 0) {
			errno.set(-ret);
			return -1;
		} else {
			return ret;
		}
	}
	
	protected void activate(ComponentContext context) {
		backdoorPath = backdoor.findBackdoorPath(getOS());
	}
	
	protected void deactivate(ComponentContext context) {
		
	}
	
	protected void setLogManager(ILogManager logManager) {
		this.logger = logManager.getLogger("System Service");
		backdoor.setLogger(logger);
	}
	protected void unsetLogManager(ILogManager logManager) {
		
	}

}
