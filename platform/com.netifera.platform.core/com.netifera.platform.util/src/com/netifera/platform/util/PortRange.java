package com.netifera.platform.util;

import java.util.Iterator;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.iterables.SequentialIterator;

public class PortRange implements Comparable<PortRange>, IndexedIterable<Integer> {

	private static final long serialVersionUID = -2063260940132141369L;
	
	protected int start;
	protected int end;
	
	public PortRange(final int port) throws IllegalArgumentException {
		this(port, port);
	}
	
	public PortRange(final int start, final int end) throws IllegalArgumentException {
		this.start = PortSet.verifyPort(start);
		this.end = PortSet.verifyPort(end);
	}
	
	public boolean contains(final int port) throws IllegalArgumentException {
		PortSet.verifyPort(port);
		return (port >= start) && (port <= end);
	}

	@Override
	public String toString() {
		if(start == end) {
			return Integer.toString(start);
		}
		return start + "-" + end;
	}
	
	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if (!(obj instanceof PortRange)){
			return false;
		}
		PortRange other = (PortRange)obj;
		return start == other.start && end == other.end;
	}
	
	@Override
	public int hashCode(){
		return start ^ (end >>> 17);
	}
	
	public int compareTo(final PortRange portrange) {
		if(start < portrange.start) {
			if (end > portrange.end) {
				return 1;
			}
			return -1;
		}
		if(start > portrange.start) {
			if (end < portrange.end) {
				return -1;
			}
			return 1;
		}
		if (end < portrange.end) {
			return -1;
		}
		if (end > portrange.end) {
			return 1;
		}
		return 0;
	}

	public Integer itemAt(final int index) {
		if (index < 0 || index >= itemCount()) {
			throw new IndexOutOfBoundsException();
		}
		return start + index;
	}

	public int itemCount() {
		return end - start + 1;
	}

	public Iterator<Integer> iterator() {
		return new SequentialIterator<Integer>(this);
	}
}
