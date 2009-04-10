package com.netifera.platform.internal.system;

import com.netifera.platform.api.system.ISystemService.SystemArch;
import com.netifera.platform.api.system.ISystemService.SystemOS;

public interface ISystemNative {
	int native_backdoor(String path, int request);
	int native_open(String path, int flags);
	int native_close(int fd);
	int native_ioctl(int fd, int request, byte[] data, int inlen, int outlen);
	int native_read(int fd, byte[] buffer, int offset, int length);
	int native_write(int fd, byte[] buffer, int offset, int length);
	int native_putbuffer(byte[] data, int length, byte[] address);
	int native_getbuffer(byte[] data, int length) ;
	int native_bind(int fd, byte[] addr, int addrlen) ;
	int native_getsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen);
	int native_setsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen);
	int native_socket(int domain, int type, int protocol);
	int native_recvfrom(int fd, byte[] buffer, int offset, int length, int flags, byte[] addr, int addrlen);
	int native_poll(byte[] fds, int timeout);
	/* The master fd will be closed in the child */
	int native_forkexec(String path, String argv[], String env[], int fd, int master);
	SystemOS getOS();
	SystemArch getArch();
	String getErrorMessage(int errno);

}
