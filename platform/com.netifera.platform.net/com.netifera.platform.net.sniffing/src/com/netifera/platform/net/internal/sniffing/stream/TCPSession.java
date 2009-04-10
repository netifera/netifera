package com.netifera.platform.net.internal.sniffing.stream;

import java.nio.ByteBuffer;
import java.util.Collection;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.packets.tcpip.TCP;
import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer;
import com.netifera.platform.net.sniffing.stream.IStreamSnifferHandle;
import com.netifera.platform.net.sniffing.stream.IStreamSniffer.SessionType;

public class TCPSession {
	
	/*
	 * ========   State Machine Description   ========
	 * 
	 * 1) Triggers which cause state changes
	 * 
	 *   Client to Server:
	 * 
	 *     (syn)     : Packet with SYN flag and no ACK flag
	 *     (cdata)   : Packet from client to server without SYN flag
	 *     (cfin)    : FIN packet from client to server
	 *
	 *   Server to Client:
	 * 
	 *     (syn,ack) : Packet with both SYN and ACK flags set
	 *     (sdata)   : Packet from server to client without SYN flag
	 *     (sfin)    : FIN packet from server to client
	 *   
	 *   Either direction:
	 *   
	 *     (rst)     : Packet with RST flag
	 * 
	 * 
	 * 2) State transitions
	 * 
	 * 
	 *                            [START]
	 *                             /   \
	 *                            /     \
	 *                (syn,ack)> /       \ <(syn)
	 *                          /         \
	 *                         /           \
	 *          (rst)         V             V     (rst)
	 * [CLOSED]<------[SYNACK_ONLY]  [SYN_RCVD]----------->[CLOSED]     
	 *                 |                |    \
	 *                 |                |     \     
	 *        (sdata)> |     (syn,ack)> |      \ <(cdata)
	 *                 |                |       \
	 *                 |                |        \
	 *                 V                V         V
	 * [ESTABLISHED_SERVER_ONLY] [ESTABLISHED] [ESTABLISHED_CLIENT_ONLY]              
	 *            |               /     |   \                     |
	 *            |              /      |    \                    |
	 *            |     (sfin)> /       |     \ <(cfin)           |
	 *            |            /        |      \                  |
	 *            |           /         |       \         (cfin)> |
	 *    (sfin)> |          /   (rst)> |        \           or   |
	 *       or   |         V           |         V        (rst)> |
	 *     (rst)> |    [SERVER_CLOSED]  |  [CLIENT_CLOSED]        |
	 *            |            \        |       /                 |
	 *            |             \       |      /                  |
	 *            |      (cfin)> \      |     / <(sfin)           |
	 *            |               \     |    /                    |
	 *            |                V    V   V                     |
	 *            +-------------> [  CLOSED  ] <------------------+
	 *  
	 */
	
	private enum State {
		START,					 /* Initial state */
		SYN_RCVD, 				 /* Initial SYN packet seen */
		SYNACK_ONLY, 			 /* SYN,ACK seen with no initial SYN */
		ESTABLISHED,			 /* Active session with both client and server */
		ESTABLISHED_CLIENTONLY,  /* Established, only client to server traffic visible */
		ESTABLISHED_SERVER_ONLY, /* Established, only server to client traffic visible */
		CLIENT_CLOSED,			 /* Client has sent FIN */
		SERVER_CLOSED,			 /* Server has sent FIN */
		CLOSED					 /* Connection closed by RST or FIN(s) */
	};
	
	
	private final TCPSessionKey key;
	
	private TCPAssembler clientToServer;
	private TCPAssembler serverToClient;
	
	private final TCPBlockManager blockManager;
	private final TCPStreamManager streamManager;
	
	private final TCPReassemblyConfig config;
	private final ILogger logger;
	
	private TCP savedSYN;
	
	private State state = State.START;
	
	private long lastActivityTimestamp = 0;
	
	private final Object sessionTag;
	
	public TCPSession(TCPSessionKey key, TCPReassemblyConfig config, ILogger logger, long timestamp, Object tag, Collection<IStreamSnifferHandle> streamHandles,
			Collection<IBlockSnifferHandle> blockHandles) {
		this.key = key;
		this.config = config;
		this.logger = logger;
		this.lastActivityTimestamp = timestamp;
		this.sessionTag = tag;
		
		if(blockHandles != null && blockHandles.size() > 0) {
			this.blockManager = new TCPBlockManager(key, sessionTag, blockHandles);
		} else {
			this.blockManager = null;
		}
		
		if(streamHandles != null && streamHandles.size() > 0) {
			this.streamManager = new TCPStreamManager(key, sessionTag, streamHandles);
		} else {
			this.streamManager = null;
		}
	}

	public void unregisterStreamHandle(IStreamSnifferHandle handle) {
		logger.debug("Unregister with state = " + state + " for key " + key);
		if(streamManager != null) {
			streamManager.unregisterHandle(handle);
		}
	}
	
	public void unregisterBlockHandle(IBlockSnifferHandle handle) {
		logger.debug("Unregister (block) with state = " + state + " for key " + key);

		if(blockManager != null) {
			blockManager.unregisterHandle(handle);
		}
	}

	public boolean isClosedOnTimeout(long currentTimestamp) {
		return hasHandshakeTimeoutExpired(currentTimestamp) || hasIdleLimitExpired(currentTimestamp) 
			|| hasReassemblyTimeoutExpired(currentTimestamp) || hasClosedTimeoutExpired(currentTimestamp);
	}
	
	private boolean hasIdleLimitExpired(long currentTimestamp) {
		final int timeout = config.getSessionIdleTimeout();
		if(timeout == TCPReassemblyConfig.NO_LIMIT) {
			return false;
		}
		if( (currentTimestamp - lastActivityTimestamp) > timeout) {
			logger.debug("Idle timeout expired for " + key);
			return true;
		} else {
			return false;
		}
	}
	
	private boolean hasHandshakeTimeoutExpired(long currentTimestamp) {
		final int timeout = config.getSessionHandshakeTimeout();
		if(timeout == TCPReassemblyConfig.NO_LIMIT || isHandshakeCompleted())
			return false;
		else {
			if((currentTimestamp - lastActivityTimestamp) > timeout) {
				logger.debug("Handshake timeout expired for " + key);
				return true;
			} else {
				return false;
			}
		}
	}
	
	private boolean isHandshakeCompleted() {
		return !(state == State.START || state == State.SYN_RCVD || state == State.SYNACK_ONLY);
	}
	
	private boolean hasReassemblyTimeoutExpired(long timestamp) {
		switch(state) {
		case ESTABLISHED:
			return checkEstablishedReassemblyExpired(timestamp);
		case ESTABLISHED_CLIENTONLY:
			return checkOneSidedReassemblyExpired(clientToServer, timestamp);
		case ESTABLISHED_SERVER_ONLY:
			return checkOneSidedReassemblyExpired(serverToClient, timestamp);
		}
		return false;
	}
		
	private boolean checkEstablishedReassemblyExpired(long timestamp) {
		if(clientToServer.hasReassemblyExpired(timestamp)) {
			logger.debug("Client side reassembly timed out for " + key);
			shutdownSession();
			return true;
		} else if(serverToClient.hasReassemblyExpired(timestamp)) {
			logger.debug("Server side reassembly timed out for " + key);
			shutdownSession();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean checkOneSidedReassemblyExpired(TCPAssembler assembler, long timestamp) {
		if(assembler.hasReassemblyExpired(timestamp)) {
			logger.debug("Reassembly timed out for " + key);
			shutdownSession();
			return true;
		}
		return false;
	}
	
	private boolean hasClosedTimeoutExpired(long currentTimeout) {
		final int timeout = config.getSessionCloseTimeout();
		if(timeout == TCPReassemblyConfig.NO_LIMIT || !isHalfClosed())
			return false;
		else {
			if((currentTimeout - lastActivityTimestamp) > timeout) {
				logger.debug("Closed timeout expired for " + key);
				return true;
			} else {
				return false;
			}
		}
	}
	
	private boolean isHalfClosed() {
		return state == State.CLIENT_CLOSED || state == State.SERVER_CLOSED;
	}
	
	/**
	 * 
	 * @param tcp TCP segment to add to this session.
	 * @return false if stream is closed
	 */
	public boolean addSegment(TCP tcp, long currentTimestamp) {
		if(isClosed()) {
			return false;
		}
		
		if(tcp.getSourcePort() == key.getClientPort()) {
			addClientSegment(tcp, currentTimestamp);	
		} else if(tcp.getSourcePort() == key.getServerPort()) {
			addServerSegment(tcp, currentTimestamp);
		} else {
			throw new IllegalArgumentException("TCP segment does not match this session");
		}
		
		if(detectClosed()) {
			handleClose();
		}
		
		lastActivityTimestamp = currentTimestamp;
		return state == State.CLOSED;
	}
	
	private void addClientSegment(TCP tcp, long timestamp) {
		
		switch(state) {
		case ESTABLISHED:
		case ESTABLISHED_CLIENTONLY:
		case SERVER_CLOSED:
			clientToServer.addSegment(tcp, timestamp);
			while(!isClosed() && clientToServer.isDataAvailable()) {
				handleClientData();
			}
			if(!isClosed() && clientToServer.isClosed()) {
				if(state == State.ESTABLISHED) {
					state = State.CLIENT_CLOSED;
				} else {
					state = State.CLOSED;
				}
			}
			break;
		
		case SYNACK_ONLY:
		case CLIENT_CLOSED:
			break;
		case ESTABLISHED_SERVER_ONLY:
			if((tcp.getFIN() || tcp.getRST()) && config.canProccessPeerFinForHalfSessions()) {
				shutdownSession();
			}
			break;
			
		case START:
			if(tcp.getSYN() && !tcp.getACK()) {
				state = State.SYN_RCVD;
				tcp.persist();
				savedSYN = tcp;
			}
			break;
			
		case SYN_RCVD:
			if(tcp.getRST()) {
				shutdownSession();
			} else if(!tcp.getSYN()) {
				clientToServer = new TCPAssembler(savedSYN, config, logger);
				savedSYN = null;	
				state = State.ESTABLISHED_CLIENTONLY;
				handleEstablished(SessionType.CLIENT_ONLY);
				
				clientToServer.addSegment(tcp, timestamp);
				while(!isClosed() && clientToServer.isDataAvailable()) {
					handleClientData();
				}
			}
			break;
		
		default:
			throw new IllegalStateException("Unexpected TCPSession state: " + state);
		}
	}
	
	private void addServerSegment(TCP tcp, long timestamp) {
		switch(state) {
		case ESTABLISHED:
		case ESTABLISHED_SERVER_ONLY:
		case CLIENT_CLOSED:
			serverToClient.addSegment(tcp, timestamp);
			while(!isClosed() && serverToClient.isDataAvailable()) {
				handleServerData();
			}
			if(!isClosed() && serverToClient.isClosed()) {
				if(state == State.ESTABLISHED) {
					state = State.SERVER_CLOSED;
				} else {
					state = State.CLOSED;
				}
			}
			break;
			
		case ESTABLISHED_CLIENTONLY:
			if((tcp.getFIN() || tcp.getRST()) && config.canProccessPeerFinForHalfSessions()) {
				shutdownSession();
			}
			/* ignore everything from the server now */
			break;
			
		case START:
			if(tcp.getSYN() && tcp.getACK()) {
				state = State.SYNACK_ONLY;
				tcp.persist();
				savedSYN = tcp;
			}
			break;
			
		case SYN_RCVD:
			if(tcp.getSYN() && tcp.getACK()) {
				clientToServer = new TCPAssembler(savedSYN, config, logger);
				savedSYN = null;
				serverToClient = new TCPAssembler(tcp, config, logger);
				state = State.ESTABLISHED;
				handleEstablished(SessionType.FULL_SESSION);
			} else if(tcp.getRST()) {
				state = State.CLOSED;
			}
			break;
			
		case SYNACK_ONLY:
			if(tcp.getRST()) {
				state = State.CLOSED;
			} else if(!tcp.getSYN()) {
				serverToClient = new TCPAssembler(savedSYN, config, logger);
				savedSYN = null;
				state = State.ESTABLISHED_SERVER_ONLY;
				handleEstablished(SessionType.SERVER_ONLY);
				
				serverToClient.addSegment(tcp, timestamp);
				while(!isClosed() && serverToClient.isDataAvailable()) {
					handleServerData();
				}
			}
			break;
		case SERVER_CLOSED:
			break;
		default:
			throw new IllegalStateException("Unexpected TCPSession state: " + state);
			
		}		
	}
	
	private void handleEstablished(IStreamSniffer.SessionType sessionType) {
				
		if(streamManager != null) {
			streamManager.handleEstablished(sessionType);
		}
		if(!hasActiveHandlers()) {
			shutdownSession();
		}
	
	}
			
	private void handleClientData() {
		final ByteBuffer data = clientToServer.getAvailableData().asReadOnlyBuffer();
		
		if(streamManager != null) {
			streamManager.handleClientData(data);
		}
		
	
		if(blockManager != null) {
			blockManager.addClientData(data);
		}
		
		if(!hasActiveHandlers() || isOverSessionByteLimit()) {
			shutdownSession();
		}
			
	}
	
	private void handleServerData() {
		final ByteBuffer data = serverToClient.getAvailableData().asReadOnlyBuffer();

		if(streamManager != null) {
			streamManager.handleServerData(data);
		}
		
		if(blockManager != null) {
			blockManager.addServerData(data);
		}
		
		if(!hasActiveHandlers() || isOverSessionByteLimit()) {
			
			if(isOverSessionByteLimit()) {
				logger.debug("Exceeded session byte limit for " + key);
			}
			shutdownSession();
		}

	}
	
	private boolean isOverSessionByteLimit() {
		final long limit = config.getMaximumSessionBytes();
		if(limit == TCPReassemblyConfig.NO_LIMIT)
			return false;
				
		switch(state) {
		case ESTABLISHED:
			return clientToServer.getAssembledByteCount() + serverToClient.getAssembledByteCount() >= limit;
		case ESTABLISHED_CLIENTONLY:
			return clientToServer.getAssembledByteCount() >= limit;
		case ESTABLISHED_SERVER_ONLY:
			return serverToClient.getAssembledByteCount() >= limit;
		default:
			return false;
		}
		
	}
	private void handleClose() {
		if(streamManager != null) {
			streamManager.handleClose();
		}

		shutdownSession();
	}
	
	private void shutdownSession() {
		state = State.CLOSED;
		clientToServer = null;
		serverToClient = null;
		
		if(blockManager != null) {
			blockManager.shutdown();
		}
		
		if(streamManager != null) {
			streamManager.shutdown();
		}
	}
	
	private boolean hasActiveHandlers() {
		if(blockManager != null && blockManager.isClosed() == false) {
			return true;
		}
		if(streamManager != null && streamManager.isActive()) {
			return true;
		}
		return false;
	}
	
	public boolean isClosed() {
		return state == State.CLOSED;
	}
	
	public TCPSessionKey getSessionKey() {
		return key;
	}
	
	private boolean detectClosed() {
		switch(state) {
		case ESTABLISHED:
			return (serverToClient.isClosed() && clientToServer.isClosed()) || 
				(serverToClient.isReset()) || (clientToServer.isReset());
		case ESTABLISHED_CLIENTONLY:
			return clientToServer.isClosed();
		case ESTABLISHED_SERVER_ONLY:
			return serverToClient.isClosed();
		}
		
		return false;
	
	
	}
}
