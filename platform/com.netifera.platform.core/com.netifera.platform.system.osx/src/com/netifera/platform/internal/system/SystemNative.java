package com.netifera.platform.internal.system;
import com.netifera.platform.api.system.ISystemService.SystemArch;
import com.netifera.platform.api.system.ISystemService.SystemOS;

public class SystemNative implements ISystemNative {
	private final static boolean ENABLE_ERROR_MESSAGES = true;
	public native int native_backdoor(String path, int request);
	public native int native_open(String path, int flags);
	public native int native_close(int fd);
	public native int native_ioctl(int fd, int request, byte[] data, int inlen, int outlen);
	public native int native_read(int fd, byte[] data, int offset, int length);
	public native int native_putbuffer(byte[] data, int length, byte[] address);
	public native int native_getbuffer(byte[] data, int length);

	
	
	static {
		try {
			System.loadLibrary("system");
		} catch(UnsatisfiedLinkError e) {
			System.err.println("Failed to load native library, java.library.path="
					+ System.getProperty("java.library.path"));
			e.printStackTrace();
		}
	}
	
	 public SystemOS getOS() {
		return SystemOS.OS_OSX;
	}
	
	 public SystemArch getArch() {
		return SystemArch.ARCH_X86;
	}
	
	 public String getErrorMessage(int errno) {
		if(ENABLE_ERROR_MESSAGES) {
		switch(errno) {
		case 1:		/* EPERM */
			 return "Operation not permitted";
		case 2:		/* ENOENT */
			 return "No such file or directory";
		case 3:		/* ESRCH */
			 return "No such process";
		case 4:		/* EINTR */
			 return "Interrupted system call";
		case 5:		/* EIO */
			 return "Input/output error";
		case 6:		/* ENXIO */
			 return "Device not configured";
		case 7:		/* E2BIG */
			 return "Argument list too long";
		case 8:		/* ENOEXEC */
			 return "Exec format error";
		case 9:		/* EBADF */
			 return "Bad file descriptor";
		case 10:		/* ECHILD */
			 return "No child processes";
		case 11:		/* EDEADLK */
			 return "Resource deadlock avoided";
		case 12:		/* ENOMEM */
			 return "Cannot allocate memory";
		case 13:		/* EACCES */
			 return "Permission denied";
		case 14:		/* EFAULT */
			 return "Bad address";
		case 15:		/* ENOTBLK */
			 return "Block device required";
		case 16:		/* EBUSY */
			 return "Device / Resource busy";
		case 17:		/* EEXIST */
			 return "File exists";
		case 18:		/* EXDEV */
			 return "Cross-device link";
		case 19:		/* ENODEV */
			 return "Operation not supported by device";
		case 20:		/* ENOTDIR */
			 return "Not a directory";
		case 21:		/* EISDIR */
			 return "Is a directory";
		case 22:		/* EINVAL */
			 return "Invalid argument";
		case 23:		/* ENFILE */
			 return "Too many open files in system";
		case 24:		/* EMFILE */
			 return "Too many open files";
		case 25:		/* ENOTTY */
			 return "Inappropriate ioctl for device";
		case 26:		/* ETXTBSY */
			 return "Text file busy";
		case 27:		/* EFBIG */
			 return "File too large";
		case 28:		/* ENOSPC */
			 return "No space left on device";
		case 29:		/* ESPIPE */
			 return "Illegal seek";
		case 30:		/* EROFS */
			 return "Read-only file system";
		case 31:		/* EMLINK */
			 return "Too many links";
		case 32:		/* EPIPE */
			 return "Broken pipe";
		case 33:		/* EDOM */
			 return "Numerical argument out of domain";
		case 34:		/* ERANGE */
			 return "Result too large";
		case 35:		/* EAGAIN */
			 return "Resource temporarily unavailable";
		case 36:		/* EINPROGRESS */
			 return "Operation now in progress";
		case 37:		/* EALREADY */
			 return "Operation already in progress";
		case 38:		/* ENOTSOCK */
			 return "Socket operation on non-socket";
		case 39:		/* EDESTADDRREQ */
			 return "Destination address required";
		case 40:		/* EMSGSIZE */
			 return "Message too long";
		case 41:		/* EPROTOTYPE */
			 return "Protocol wrong type for socket";
		case 42:		/* ENOPROTOOPT */
			 return "Protocol not available";
		case 43:		/* EPROTONOSUPPORT */
			 return "Protocol not supported";
		case 44:		/* ESOCKTNOSUPPORT */
			 return "Socket type not supported";
		case 45:		/* ENOTSUP */
			 return "Operation not supported";
		case 46:		/* EPFNOSUPPORT */
			 return "Protocol family not supported";
		case 47:		/* EAFNOSUPPORT */
			 return "Address family not supported by protocol family";
		case 48:		/* EADDRINUSE */
			 return "Address already in use";
		case 49:		/* EADDRNOTAVAIL */
			 return "Can't assign requested address";
		case 50:		/* ENETDOWN */
			 return "Network is down";
		case 51:		/* ENETUNREACH */
			 return "Network is unreachable";
		case 52:		/* ENETRESET */
			 return "Network dropped connection on reset";
		case 53:		/* ECONNABORTED */
			 return "Software caused connection abort";
		case 54:		/* ECONNRESET */
			 return "Connection reset by peer";
		case 55:		/* ENOBUFS */
			 return "No buffer space available";
		case 56:		/* EISCONN */
			 return "Socket is already connected";
		case 57:		/* ENOTCONN */
			 return "Socket is not connected";
		case 58:		/* ESHUTDOWN */
			 return "Can't send after socket shutdown";
		case 59:		/* ETOOMANYREFS */
			 return "Too many references: can't splice";
		case 60:		/* ETIMEDOUT */
			 return "Operation timed out";
		case 61:		/* ECONNREFUSED */
			 return "Connection refused";
		case 62:		/* ELOOP */
			 return "Too many levels of symbolic links";
		case 63:		/* ENAMETOOLONG */
			 return "File name too long";
		case 64:		/* EHOSTDOWN */
			 return "Host is down";
		case 65:		/* EHOSTUNREACH */
			 return "No route to host";
		case 66:		/* ENOTEMPTY */
			 return "Directory not empty";
		case 67:		/* EPROCLIM */
			 return "Too many processes";
		case 68:		/* EUSERS */
			 return "Too many users";
		case 69:		/* EDQUOT */
			 return "Disc quota exceeded";
		case 70:		/* ESTALE */
			 return "Stale NFS file handle";
		case 71:		/* EREMOTE */
			 return "Too many levels of remote in path";
		case 72:		/* EBADRPC */
			 return "RPC struct is bad";
		case 73:		/* ERPCMISMATCH */
			 return "RPC version wrong";
		case 74:		/* EPROGUNAVAIL */
			 return "RPC prog. not avail";
		case 75:		/* EPROGMISMATCH */
			 return "Program version wrong";
		case 76:		/* EPROCUNAVAIL */
			 return "Bad procedure for program";
		case 77:		/* ENOLCK */
			 return "No locks available";
		case 78:		/* ENOSYS */
			 return "Function not implemented";
		case 79:		/* EFTYPE */
			 return "Inappropriate file type or format";
		case 80:		/* EAUTH */
			 return "Authentication error";
		case 81:		/* ENEEDAUTH */
			 return "Need authenticator";
		case 82:		/* EPWROFF */
			 return "Device power is off";
		case 83:		/* EDEVERR */
			 return "Device error, e.g. paper out";
		case 84:		/* EOVERFLOW */
			 return "Value too large to be stored in data type";
		case 85:		/* EBADEXEC */
			 return "Bad executable";
		case 86:		/* EBADARCH */
			 return "Bad CPU type in executable";
		case 87:		/* ESHLIBVERS */
			 return "Shared library version mismatch";
		case 88:		/* EBADMACHO */
			 return "Malformed Macho file";
		case 89:		/* ECANCELED */
			 return "Operation canceled";
		case 90:		/* EIDRM */
			 return "Identifier removed";
		case 91:		/* ENOMSG */
			 return "No message of desired type";
		case 92:		/* EILSEQ */
			 return "Illegal byte sequence";
		case 93:		/* ENOATTR */
			 return "Attribute not found";
		case 94:		/* EBADMSG */
			 return "Bad message";
		case 95:		/* EMULTIHOP */
			 return "Reserved";
		case 96:		/* ENODATA */
			 return "No message available on STREAM";
		case 97:		/* ENOLINK */
			 return "Reserved";
		case 98:		/* ENOSR */
			 return "No STREAM resources";
		case 99:		/* ENOSTR */
			 return "Not a STREAM";
		case 100:		/* EPROTO */
			 return "Protocol error";
		case 101:		/* ETIME */
			 return "STREAM ioctl timeout";
		case 102:		/* EOPNOTSUPP */
			 return "Operation not supported on socket";
		case 103:		/* ENOPOLICY */
			 return "No such policy registered";
		}
		}
		return "Errno = " + errno;
	}
	 
	private int fail(String method) {
		throw new IllegalStateException("System method (" + method + ") not implemented");
	}

	public int native_bind(int fd, byte[] addr, int addrlen) {
		return fail("native_bind");
	}
	public int native_getsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen) {
		return fail("native_getsockopt");
	}
	public int native_setsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen) {
		return fail("native_setsockopt");
	}
	public int native_socket(int domain, int type, int protocol) {
		return fail("native_socket");
	}
	public int native_recvfrom(int fd, byte[] buffer, int offset, int length,
			int flags, byte[] addr, int addrlen) {
		return fail("native_recvfrom");
	}
	public int native_poll(byte[] fds, int timeout) {
		return fail("native_poll");
	}
	
	public int native_forkexec(String path, String argv[], String env[], int fd, int master) {
		return fail("native_forkexec");
	}
	public int native_write(int fd, byte[] buffer, int offset, int length) {
		return fail("native_write");
	}

}

