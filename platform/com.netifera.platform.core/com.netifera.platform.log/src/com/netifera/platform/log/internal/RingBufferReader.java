package com.netifera.platform.log.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.netifera.platform.api.log.ILogEntry;
import com.netifera.platform.api.log.ILogReader;

public class RingBufferReader implements ILogReader, Iterable<ILogEntry> {
	private final static int SIZE = 100;
	final private List<ILogEntry> ringBuffer = new ArrayList<ILogEntry>(100);
	int writeIndex; 
	int readIndex;
	
	public void log(ILogEntry entry) {
		ringBuffer.add(writeIndex, entry);
		writeIndex = (writeIndex + 1) % SIZE;
		if(readIndex == writeIndex) {
			readIndex = (readIndex + 1) % SIZE;
		}		
	}
	public void logRaw(String message) {
	}

	public static class BufferIterator implements Iterator<ILogEntry> {
		private List<ILogEntry> buffer;
		private int ridx;
		private int widx;
		BufferIterator(List<ILogEntry> buffer, int ridx, int widx) {
			this.buffer = new ArrayList<ILogEntry>(buffer);
			this.ridx = ridx;
			this.widx = widx;
		}
		public boolean hasNext() {
			return ridx != widx;
		}
		public ILogEntry next() {
			final ILogEntry entry = buffer.get(ridx);
			ridx = (ridx + 1) % SIZE;
			return entry;
		}
		public void remove() {
			throw new UnsupportedOperationException();			
		}
	}

	public Iterator<ILogEntry> iterator() {
		return new BufferIterator(ringBuffer, readIndex, writeIndex);
	}
	
	

	

}
