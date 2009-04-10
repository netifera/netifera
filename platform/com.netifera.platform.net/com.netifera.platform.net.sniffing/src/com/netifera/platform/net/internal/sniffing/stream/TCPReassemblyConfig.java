package com.netifera.platform.net.internal.sniffing.stream;

public class TCPReassemblyConfig {
	private final static int DEFAULT_MAXIMUM_SESSION_COUNT = 500;
	private final static int DEFAULT_MAXIMUM_SESSION_BYTES = 1024 * 1024;
	private final static int DEFAULT_MAXIMUM_PENDING_REASSEMBLY_BYTES = 100 * 1024;
	private final static int DEFAULT_SESSION_IDLE_TIMEOUT = 300;
	private final static int DEFAULT_SESSION_HANDSHAKE_TIMEOUT = 10;
	private final static int DEFAULT_SESSION_CLOSE_TIMEOUT = 10;
	private final static int DEFAULT_REASSEMBLY_TIMEOUT = 60;
	private final static boolean DEFAULT_PROCESS_FIN_OUTSIDE_REASSEMBLY = true;
	private final static boolean DEFAULT_PROCESS_PEER_FIN_FOR_HALF_SESSIONS = true;
	
	public final static int NO_LIMIT = -1;
	
	private int maximumSessionCount = DEFAULT_MAXIMUM_SESSION_COUNT;
	
	/* byte sized limits */
	private int maximumSessionBytes = DEFAULT_MAXIMUM_SESSION_BYTES;
	private int maximumPendingReassemblyBytes = DEFAULT_MAXIMUM_PENDING_REASSEMBLY_BYTES;
	
	/* timeouts in seconds */
	private int sessionIdleTimeout = DEFAULT_SESSION_IDLE_TIMEOUT;
	private int sessionHandshakeTimeout = DEFAULT_SESSION_HANDSHAKE_TIMEOUT;
	private int sessionCloseTimeout = DEFAULT_SESSION_CLOSE_TIMEOUT;
	private int reassemblyTimeout = DEFAULT_REASSEMBLY_TIMEOUT;
	
	private boolean processFinOutsideReassembly = DEFAULT_PROCESS_FIN_OUTSIDE_REASSEMBLY;
	private boolean processPeerFinForHalfSessions = DEFAULT_PROCESS_PEER_FIN_FOR_HALF_SESSIONS;

	
	/**
	 * The maximum number of sessions that will be tracked simultaneously on a single interface.  A newly detected
	 * session will be ignored if the current number of open sessions has reached this limit.
	 * 
	 * @return The maximum number of open sessions.
	 */
	public int getMaximumSessionCount() {
		return maximumSessionCount;
	}
	
	/**
	 * The maximum total number of bytes in both directions for a session.  When this limit is exceeded 
	 * the TCP reassembly engine will close the session and stop tracking it.  
	 * 
	 * This value can be set to -1 to indicate that there is not a limit.
	 * 
	 * @return The maximum number of bytes of traffic in a session before automatically closing it, or -1 for
	 * no limit. 
	 */
	public int getMaximumSessionBytes() {
		return maximumSessionBytes;
	}
	
	/**
	 * The maximum number of bytes of data which can not yet be reassembled because of missing data in the stream. 
	 * When this limit is exceeded the session will automatically be closed.  
	 * 
	 * This value can be set to -1 to indicate that there is not a limit.
	 * 
	 * @return The maximum number of pending reassembly bytes, or -1 for no limit.
	 */
	public int getMaximumPendingReassemblyBytes() {
		return maximumPendingReassemblyBytes;
	}
	
	/**
	 * The maximum time in seconds that a TCP session will be tracked after not seeing any traffic in either
	 * direction.  
	 * 
	 * This value can be set to -1 to indicate that there is no timeout.
	 * 
	 * @return The maximum idle period in seconds for a session, or -1 for no timeout.
	 */
	public int getSessionIdleTimeout() {
		return sessionIdleTimeout;	
	}
	
	/**
	 * The maximum time in seconds that a partial TCP connection handshake will be tracked without completion.
	 * This timeout prevents dropped handshake packets or aborted handshakes from holding a session open forever.
	 * 
	 * This value can be set to -1 to indicate that there is no timeout.
	 * 
	 * @return The maximum time in seconds that a TCP handshake will be tracked without completion, or -1 for 
	 * no timeout.
	 */
	
	public int getSessionHandshakeTimeout() {
		return sessionHandshakeTimeout;
	}
	
	/**
	 * The maximum time in seconds that the reassembly engine will wait for FIN from one side of a connection 
	 * after receiving a FIN from the other side.  This timeout protects against dropped FIN packets holding
	 * a session open forever.
	 * 
	 * This value can be set to -1 to indicate that there is no timeout.
	 * 
	 * @return The maximum time in seconds that a session will be tracked after receiving a FIN packet from only
	 * one side of the connection, or -1 for no timeout.
	 */
	public int getSessionCloseTimeout() {
		return sessionCloseTimeout;
	}
	
	/**
	 * The maximum time in seconds that the TCP reassembly engine will track a session without outstanding data to
	 * be reassembled because of missing or dropped packets.  
	 * 
	 * This value can be set to -1 to indicate that there is no timeout.
	 * 
	 * @return The maximum time in seconds that the reassembly session will wait for missing packets to arrive
	 * which are needed for reassembly, or -1 for no timeout.
	 */
	public int getReassemblyTimeout() {
		return reassemblyTimeout;
	}
	
	
	/**
	 * This setting controls the behavior of the TCP reassembly engine when a FIN or RST segment arrives that cannot
	 * be processed immediately because there is pending data to be assembled due to missing, dropped, or reordered
	 * packets.  If this flag is true, the FIN or RST will be processed immediately rather than being placed in the 
	 * queue of data waiting to be reassembled.  This protects against dropped packets causing a FIN or RST from
	 * never being processed and the session remaining open permanently.
	 * 
	 * @return True if this feature should be enabled.
	 */
	public boolean canProcessFinOutsideReassembly() {
		return processFinOutsideReassembly;
	}
	
	/**
	 * This setting controls the behavior of the TCP reassembly engine when only one side of the session is being
	 * tracked and a FIN or RST is received on the other side.  This can happen when many packets are dropped, and
	 * this is an additional defense against dropped packets causing a session to remain open forever.
	 *
	 * @return
	 */
	public boolean canProccessPeerFinForHalfSessions() {
		return processPeerFinForHalfSessions;
	}
	

}
