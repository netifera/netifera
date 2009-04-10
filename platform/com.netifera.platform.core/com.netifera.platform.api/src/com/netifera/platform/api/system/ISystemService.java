package com.netifera.platform.api.system;


public interface ISystemService {
	public static final String BACKDOOR_PATH_PROPERTY = "backdoor.path";
	
	enum SystemOS { OS_LINUX, OS_OSX };
	
	enum SystemArch { ARCH_X86 };
	
	int backdoor_request(int request);
	
	String backdoor_path();
	
	int syscall_open(String path, int flags);
	
	int syscall_read(int fd, byte[] buffer, int offset, int length);
	
	int syscall_write(int fd, byte[] buffer, int offset, int length);
	
	int syscall_close(int fd);
	
	/**
	 * Perform an ioctl request on a file descriptor.  
	 * 
	 * @param fd  File descriptor returned from a call to open() or socket()
	 * @param request Request constant.
	 * @param data Buffer to pass or receive data from system call.
	 * @param inlen Bytes to copy from <code>data</data> to internal buffer before calling native system call.
	 * @param outlen Bytes to copy from internal buffer to <code>data</code> after calling native system call.
	 * @return
	 */
	int syscall_ioctl(int fd, int request, byte[] data, int inlen, int outlen);

	int syscall_socket(int domain, int type, int protocol);
	
	// XXX addrlen needed?
	int syscall_bind(int fd, byte[] addr, int addrlen);
	// XXX optlen needed?
	int syscall_getsockopt(int fd, int level, int optname, byte[] optdata, int optlen);
	int syscall_setsockopt(int fd, int level, int optname, byte[] optdata, int optlen);
	int syscall_recvfrom(int fd, byte[] buffer, int offset, int length, int flags, byte[] addr, int addrlen);
	int syscall_poll(byte[] fds, int timeout);
	
	
	/**
	 * Calls the fork() system call and in the child process the binary at
	 * <code>path</path> is executed after duplicating <code>fd</code> to
	 * stdin, stdout, and stderr.
	 * 
	 * @param path The path to the binary to execute.
	 * @param fd A file descritor which will become stdin, stdout, stderr of the new process
	 * @param master If not -1 this descriptor will be closed in the child
	 * @return The process id of the new process.
	 */
	int system_forkexec(String path, String argv[], String env[], int fd, int master);
	/**
	 * Write <code>length</code> bytes from the array
	 * <code>data</data> to the internal
	 * native static buffer and write the address of the buffer in native byte order to
	 * <code>address</code>. The <code>address</code> array must have a size
	 * equal to or greater than the pointer size of the native platform.
	 * 
	 * The native JNI library provides access to a 4096 byte static buffer which
	 * can be written to with this method and read with
	 * {@link #system_getbuffer(byte[], int)}.
	 * 
	 * This functionality is provided in order to support ioctl() calls which
	 * require a structure containing a pointer to another structure or buffer.
	 * Since the internal buffer is statically allocated and shared, usage of
	 * these methods must use proper synchronization or locking when threaded
	 * access is possible.
	 * 
	 * @param data
	 *            Bytes to write to the internal buffer
	 * @param length
	 *            Number of bytes from <code>data</code> to write.
	 * @param address
	 *            Byte array where the native address of the internal buffer
	 *            will be written in native byte order. Must be at least as
	 *            large as the native pointer size.
	 * @return Returns 0 on success or -1 on error. It is an error if
	 *         <code>length</code> is greater than the size of the internal
	 *         native buffer or if it is greater than <code>data.length</code>.
	 *         It is also an error if <code>address.length</code> is less than
	 *         the native pointer size.
	 */
	int system_putbuffer(byte[] data, int length, byte[] address);

	/**
	 * Read <code>length</code> bytes from the internal native static buffer and
	 * store them in <code>data</code>.
	 * 
	 * The native JNI library provides access to a 4096 byte static buffer which
	 * can be written to with {@link #system_putbuffer(byte[], int, byte[])} or
	 * read from with this method.
	 * 
	 * This functionality is provided in order to support ioctl() calls which
	 * require a structure containing a pointer to another structure or buffer.
	 * Since the internal buffer is statically allocated and shared, usage of
	 * these methods must use proper synchronization or locking when threaded
	 * access is possible.
	 * 
	 * @param data
	 *            Bytes read from the internal buffer are stored here.
	 * @param length
	 *            Number of bytes to read from the internal buffer.
	 * @return Returns 0 on success or -1 on error. It is an error for
	 *         <code>length</code> to be greater than the size of the internal
	 *         buffer or greater than <code>data.length</code>.
	 */
	int system_getbuffer(byte[] data, int length);
	
	/**
	 * Read a 16 bit value from the byte array <code>data</code> in native byte order and
	 * return the value as an integer.
	 * 
	 * @param data Byte array to read from.
	 * @param offset Offset into byte array <code>data</code>.
	 * @return The 16 bit value at <code>offset</code> in byte array <code>data</code>.
	 */
	int unpack16(byte[] data, int offset);
	
	/**
	 * Read a 32 bit value from the byte array <code>data</code> in native byte order and
	 * return the value as an integer.
	 * 
	 * @param data Byte array to read from.
	 * @param offset Offset into byte array <code>data</code>.
	 * @return The 32 bit value at <code>offset</code> in byte array <code>data</code>.
	 */
	int unpack32(byte[] data, int offset);
	
	/**
	 * Store a 16 bit value at the specified offset in the byte array <code>data</code> in native
	 * bite order.
	 * 
	 * @param data Byte array where value is stored.
	 * @param offset Offset in bytes into byte array <code>data</code>.
	 * @param value The 16 bit value to be stored.
	 */
	void pack16(byte[] data, int offset, int value);
	
	/**
	 * Store a 32 bit value at the specified offset in the byte array <code>data</code> in native
	 * bite order.
	 * 
	 * @param data Byte array where value is stored.
	 * @param offset Offset in bytes into byte array <code>data</code>.
	 * @param value The 32 bit value to be stored.
	 */
	void pack32(byte[] data, int offset, int value);

	/**
	 * Convert a 16 bit integer in network byte order to host byte order.  If the system native byte order
	 * is the same as network byte order (big endian) this method returns the argument unchanged.  Otherwise
	 * the two least significant bytes of <code>n</code> are swapped to produce the return value.
	 * 
	 * @param n A 16 bit value in network byte order.
	 * @return The equivalent of <code>n</code> in host byte order.
	 */
	int ntohs(int n);
	
	/**
	 * Convert a 32 bit integer in network byte order to host byte order.  If the system native byte order
	 * is the same as network byte order (big endian) this method returns the argument unchanged.  Otherwise
	 * the order of the bytes composing <code>n</code> are reversed to produce the return value. 
	 * 
	 * @param n A 32 bit value in network byte order.
	 * @return The equivalent of <code>n</code> in host byte order.
	 */
	int ntohl(int n);
	
	/**
	 * Convert a 16 bit integer in host byte order to network byte order.  If the system native byte order
	 * is the same as network byte order (big endian) this method returns the argument unchanged.  Otherwise
	 * the two least significant bytes of <code>n</code> are swapped to produce the return value.
	 * 
	 * @param n A 16 bit value in host byte order.
	 * @return The equivalent of <code>n</code> in network byte order.
	 */
	int htons(int n);
	
	/**
	 * Convert a 32 bit integer in host byte order to network byte order.  If the system native byte order
	 * is the same as network byte order (big endian) this method returns the argument unchanged.  Otherwise
	 * the order of the bytes composing <code>n</code> are reversed to produce the return value. 
	 * 
	 * @param n A 32 bit value in host byte order.
	 * @return The equivalent of <code>n</code> in network byte order.
	 */
	int htonl(int n);
	
	/**
	 * Returns the native integer constant value for the <i>errno</i> of the last failed system call.  If no
	 * system call has failed, the returned value is <code>0</code>.  After a failed system call, this value
	 * will not be reset to <code>0</code> by a subsequent successful system call.
	 * 
	 * @return The <i>errno</i> value from the last failed system call or <code>0</code> if no system call has failed.
	 */
	int getErrno();
	
	/**
	 * Returns a string representation of the specified native integer <i>errno</i> constant.
	 * 
	 * @param errno The native integer constant to produce an error message for.
	 * @return A string message describing the error.
	 */
	String getErrorMessage(int errno);
	
	/**
	 * Returns the native operating system type.
	 * 
	 * @return The native operating system type.
	 */
	SystemOS getOS();
	
	/**
	 * Returns the native hardware architecture.
	 * 
	 * @return The native hardware architecture.
	 */
	SystemArch getArch();


}

