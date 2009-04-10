package com.netifera.platform.net.internal.sniffing.stream;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.netifera.platform.net.sniffing.stream.IBlockSnifferHandle;
import com.netifera.platform.net.sniffing.stream.ISessionKey;

public class TCPBlockManager {
	

	private final Collection<IBlockSnifferHandle> handles;
		
	private final ISessionKey key;
	private ByteBuffer clientBuffer;
	private ByteBuffer serverBuffer;
	private final Object sessionTag;
	private boolean closed;
	
	TCPBlockManager(ISessionKey key, Object sessionTag, Collection<IBlockSnifferHandle> handles) {
		this.handles = new HashSet<IBlockSnifferHandle>(handles);
		this.key = key;
		this.sessionTag = sessionTag;
		
		if(handles.size() > 0) {
			allocateBuffers();
		} else {
			closed = true;
		}	
	}
	
	void unregisterHandle(IBlockSnifferHandle handle) {
		synchronized(handles) {
			handles.remove(handle);
		}
	}
	
	void addClientData(ByteBuffer data) {
		addData(clientBuffer, data);
	}
	
	void addServerData(ByteBuffer data) {
		addData(serverBuffer, data);
	}
	
	private void addData(ByteBuffer targetBuffer, ByteBuffer dataBuffer) {
		dataBuffer.remaining();
		if(!targetBuffer.hasRemaining()) {
			return;
		} else if(targetBuffer.remaining() >= dataBuffer.remaining()) {
			targetBuffer.put(dataBuffer);
		} else {
			final int saveLimit = dataBuffer.limit();
			dataBuffer.limit(targetBuffer.remaining());
			targetBuffer.put(dataBuffer);
			dataBuffer.limit(saveLimit);
		}
		dataBuffer.rewind();
		
		for(Iterator<IBlockSnifferHandle> itr = handles.iterator(); itr.hasNext(); ) {
			IBlockSnifferHandle h = itr.next();
			if(isOverLimit(h)) {
				processHandle(h);
				itr.remove();
			}
		}	
		
	}
	private boolean isOverLimit(IBlockSnifferHandle handle) {
		final int clientSize = clientBuffer.position();
		final int serverSize = serverBuffer.position();
		final int totalSize = clientSize + serverSize;
		
		return  checkLimit(clientSize, handle.getClientLimit()) ||
				checkLimit(serverSize, handle.getServerLimit()) ||
				checkLimit(totalSize, handle.getTotalLimit());
	}
	
	private boolean checkLimit(int current, int limit) {
		return (limit > 0 && current >= limit);
	}
	
	private void processHandle(IBlockSnifferHandle handle) {
		handle.getSniffer().handleBlock(new TCPSessionContext(key, sessionTag), 
				(ByteBuffer) clientBuffer.asReadOnlyBuffer().flip(), 
				(ByteBuffer) serverBuffer.asReadOnlyBuffer().flip());
	}
	
	boolean isClosed() {
		return closed;
	}
	
	void shutdown() {
		closed = true;
		if(handles.size() > 0 && (clientBuffer.position() > 0 || serverBuffer.position() > 0)) {
			synchronized(handles) {
				for(IBlockSnifferHandle h : handles) {
					processHandle(h);
				}
			}
		}
		clientBuffer = null;
		serverBuffer = null;
		handles.clear();
	}
	
	private static int max(int a, int b) {
		return (a > b) ? (a) : (b);
	}
	
	private void allocateBuffers() {
		int maxClientLimit = 0;
		int maxServerLimit = 0;
		int maxTotalLimit = 0;
		
		for(IBlockSnifferHandle h : handles) {
			maxClientLimit = max(maxClientLimit, h.getClientLimit());
			maxServerLimit = max(maxServerLimit, h.getServerLimit());
			maxTotalLimit = max(maxTotalLimit, h.getTotalLimit());
		}
		
		final int clientBufferSize = max(maxClientLimit, maxTotalLimit);
		final int serverBufferSize = max(maxServerLimit, maxTotalLimit);
		
		
		clientBuffer = ByteBuffer.allocate(clientBufferSize);
		serverBuffer = ByteBuffer.allocate(serverBufferSize);
		
	}
	
	

}
