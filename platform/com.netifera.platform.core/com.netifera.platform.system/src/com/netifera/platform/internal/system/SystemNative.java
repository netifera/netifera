package com.netifera.platform.internal.system;

import com.netifera.platform.api.system.ISystemService.SystemArch;
import com.netifera.platform.api.system.ISystemService.SystemOS;

public class SystemNative implements ISystemNative {
	private static final boolean ENABLE_ERROR_MESSAGES = true;
	public native int native_backdoor(String path, int request);
	public native int native_socket(int domain, int type, int protocol);
	public native int native_ioctl(int fd, int request, byte[] data, int inlen, int outlen);
	public native int native_recvfrom(int fd, byte[] buffer, int offset, int length, int flags, byte[] addr, int addrlen); 
	public native int native_bind(int fd, byte[] addr, int addrlen);
	public native int native_getsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen);
	public native int native_setsockopt(int fd, int level, int optname,
			byte[] optdata, int optlen);
	public native int native_close(int fd);
	public native int native_poll(byte[] fds, int timeout);
	public native int native_forkexec(String path, String argv[], String env[], int fd, int master);
	public native int native_open(String path, int flags);
	public native int native_read(int fd, byte[] buffer, int offset, int length);
	public native int native_write(int fd, byte[] buffer, int offset, int length);
	
	static {
		try {
			System.loadLibrary("system");
		} catch(UnsatisfiedLinkError e) {
			System.err.println("Failed to load native library, java.library.path="
					+ System.getProperty("java.library.path"));
			e.printStackTrace();
		}
	}
	
	public SystemArch getArch() {
		return SystemArch.ARCH_X86;
	}
	
	public SystemOS getOS() {
		return SystemOS.OS_LINUX;
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
				 return "I/O error";
			case 6:		/* ENXIO */
				 return "No such device or address";
			case 7:		/* E2BIG */
				 return "Argument list too long";
			case 8:		/* ENOEXEC */
				 return "Exec format error";
			case 9:		/* EBADF */
				 return "Bad file number";
			case 10:		/* ECHILD */
				 return "No child processes";
			case 11:		/* EAGAIN */
				 return "Try again";
			case 12:		/* ENOMEM */
				 return "Out of memory";
			case 13:		/* EACCES */
				 return "Permission denied";
			case 14:		/* EFAULT */
				 return "Bad address";
			case 15:		/* ENOTBLK */
				 return "Block device required";
			case 16:		/* EBUSY */
				 return "Device or resource busy";
			case 17:		/* EEXIST */
				 return "File exists";
			case 18:		/* EXDEV */
				 return "Cross-device link";
			case 19:		/* ENODEV */
				 return "No such device";
			case 20:		/* ENOTDIR */
				 return "Not a directory";
			case 21:		/* EISDIR */
				 return "Is a directory";
			case 22:		/* EINVAL */
				 return "Invalid argument";
			case 23:		/* ENFILE */
				 return "File table overflow";
			case 24:		/* EMFILE */
				 return "Too many open files";
			case 25:		/* ENOTTY */
				 return "Not a typewriter";
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
				 return "Math argument out of domain of func";
			case 34:		/* ERANGE */
				 return "Math result not representable";
			case 35:		/* EDEADLK */
				 return "Resource deadlock would occur";
			case 36:		/* ENAMETOOLONG */
				 return "File name too long";
			case 37:		/* ENOLCK */
				 return "No record locks available";
			case 38:		/* ENOSYS */
				 return "Function not implemented";
			case 39:		/* ENOTEMPTY */
				 return "Directory not empty";
			case 40:		/* ELOOP */
				 return "Too many symbolic links encountered";
			case 42:		/* ENOMSG */
				 return "No message of desired type";
			case 43:		/* EIDRM */
				 return "Identifier removed";
			case 44:		/* ECHRNG */
				 return "Channel number out of range";
			case 45:		/* EL2NSYNC */
				 return "Level 2 not synchronized";
			case 46:		/* EL3HLT */
				 return "Level 3 halted";
			case 47:		/* EL3RST */
				 return "Level 3 reset";
			case 48:		/* ELNRNG */
				 return "Link number out of range";
			case 49:		/* EUNATCH */
				 return "Protocol driver not attached";
			case 50:		/* ENOCSI */
				 return "No CSI structure available";
			case 51:		/* EL2HLT */
				 return "Level 2 halted";
			case 52:		/* EBADE */
				 return "Invalid exchange";
			case 53:		/* EBADR */
				 return "Invalid request descriptor";
			case 54:		/* EXFULL */
				 return "Exchange full";
			case 55:		/* ENOANO */
				 return "No anode";
			case 56:		/* EBADRQC */
				 return "Invalid request code";
			case 57:		/* EBADSLT */
				 return "Invalid slot";
			case 59:		/* EBFONT */
				 return "Bad font file format";
			case 60:		/* ENOSTR */
				 return "Device not a stream";
			case 61:		/* ENODATA */
				 return "No data available";
			case 62:		/* ETIME */
				 return "Timer expired";
			case 63:		/* ENOSR */
				 return "Out of streams resources";
			case 64:		/* ENONET */
				 return "Machine is not on the network";
			case 65:		/* ENOPKG */
				 return "Package not installed";
			case 66:		/* EREMOTE */
				 return "Object is remote";
			case 67:		/* ENOLINK */
				 return "Link has been severed";
			case 68:		/* EADV */
				 return "Advertise error";
			case 69:		/* ESRMNT */
				 return "Srmount error";
			case 70:		/* ECOMM */
				 return "Communication error on send";
			case 71:		/* EPROTO */
				 return "Protocol error";
			case 72:		/* EMULTIHOP */
				 return "Multihop attempted";
			case 73:		/* EDOTDOT */
				 return "RFS specific error";
			case 74:		/* EBADMSG */
				 return "Not a data message";
			case 75:		/* EOVERFLOW */
				 return "Value too large for defined data type";
			case 76:		/* ENOTUNIQ */
				 return "Name not unique on network";
			case 77:		/* EBADFD */
				 return "File descriptor in bad state";
			case 78:		/* EREMCHG */
				 return "Remote address changed";
			case 79:		/* ELIBACC */
				 return "Can not access a needed shared library";
			case 80:		/* ELIBBAD */
				 return "Accessing a corrupted shared library";
			case 81:		/* ELIBSCN */
				 return ".lib section in a.out corrupted";
			case 82:		/* ELIBMAX */
				 return "Attempting to link in too many shared libraries";
			case 83:		/* ELIBEXEC */
				 return "Cannot exec a shared library directly";
			case 84:		/* EILSEQ */
				 return "Illegal byte sequence";
			case 85:		/* ERESTART */
				 return "Interrupted system call should be restarted";
			case 86:		/* ESTRPIPE */
				 return "Streams pipe error";
			case 87:		/* EUSERS */
				 return "Too many users";
			case 88:		/* ENOTSOCK */
				 return "Socket operation on non-socket";
			case 89:		/* EDESTADDRREQ */
				 return "Destination address required";
			case 90:		/* EMSGSIZE */
				 return "Message too long";
			case 91:		/* EPROTOTYPE */
				 return "Protocol wrong type for socket";
			case 92:		/* ENOPROTOOPT */
				 return "Protocol not available";
			case 93:		/* EPROTONOSUPPORT */
				 return "Protocol not supported";
			case 94:		/* ESOCKTNOSUPPORT */
				 return "Socket type not supported";
			case 95:		/* EOPNOTSUPP */
				 return "Operation not supported on transport endpoint";
			case 96:		/* EPFNOSUPPORT */
				 return "Protocol family not supported";
			case 97:		/* EAFNOSUPPORT */
				 return "Address family not supported by protocol";
			case 98:		/* EADDRINUSE */
				 return "Address already in use";
			case 99:		/* EADDRNOTAVAIL */
				 return "Cannot assign requested address";
			case 100:		/* ENETDOWN */
				 return "Network is down";
			case 101:		/* ENETUNREACH */
				 return "Network is unreachable";
			case 102:		/* ENETRESET */
				 return "Network dropped connection because of reset";
			case 103:		/* ECONNABORTED */
				 return "Software caused connection abort";
			case 104:		/* ECONNRESET */
				 return "Connection reset by peer";
			case 105:		/* ENOBUFS */
				 return "No buffer space available";
			case 106:		/* EISCONN */
				 return "Transport endpoint is already connected";
			case 107:		/* ENOTCONN */
				 return "Transport endpoint is not connected";
			case 108:		/* ESHUTDOWN */
				 return "Cannot send after transport endpoint shutdown";
			case 109:		/* ETOOMANYREFS */
				 return "Too many references: cannot splice";
			case 110:		/* ETIMEDOUT */
				 return "Connection timed out";
			case 111:		/* ECONNREFUSED */
				 return "Connection refused";
			case 112:		/* EHOSTDOWN */
				 return "Host is down";
			case 113:		/* EHOSTUNREACH */
				 return "No route to host";
			case 114:		/* EALREADY */
				 return "Operation already in progress";
			case 115:		/* EINPROGRESS */
				 return "Operation now in progress";
			case 116:		/* ESTALE */
				 return "Stale NFS file handle";
			case 117:		/* EUCLEAN */
				 return "Structure needs cleaning";
			case 118:		/* ENOTNAM */
				 return "Not a XENIX named type file";
			case 119:		/* ENAVAIL */
				 return "No XENIX semaphores available";
			case 120:		/* EISNAM */
				 return "Is a named type file";
			case 121:		/* EREMOTEIO */
				 return "Remote I/O error";
			case 122:		/* EDQUOT */
				 return "Quota exceeded";
			case 123:		/* ENOMEDIUM */
				 return "No medium found";
			case 124:		/* EMEDIUMTYPE */
				 return "Wrong medium type";
			case 125:		/* ECANCELED */
				 return "Operation Canceled";
			case 126:		/* ENOKEY */
				 return "Required key not available";
			case 127:		/* EKEYEXPIRED */
				 return "Key has expired";
			case 128:		/* EKEYREVOKED */
				 return "Key has been revoked";
			case 129:		/* EKEYREJECTED */
				 return "Key was rejected by service";
			case 130:		/* EOWNERDEAD */
				 return "Owner died";
			case 131:		/* ENOTRECOVERABLE */
				 return "State not recoverable";

			}
			}
		return "Errno = " + errno;
	}
	
	private int fail(String method) {
		throw new IllegalStateException("System method (" + method + ") not implemented");
	}
	
	public int native_getbuffer(byte[] data, int length) {
		return fail("native_getbuffer");
	}
	
	public int native_putbuffer(byte[] data, int length, byte[] address) {
		return fail("native_putbuffer");
	}

}
