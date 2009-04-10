package com.netifera.platform.net.internal.sniffing.reassembly;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.netifera.platform.net.packets.tcpip.IP;
import com.netifera.platform.net.packets.tcpip.IPFragment;

class IPReassemblyQueue {
	final private SortedSet<IPFragment> fragments =
		new TreeSet<IPFragment>(new Comparator<IPFragment>() {
			public int compare(IPFragment o1, IPFragment o2) {
				return Integer.valueOf(o1.getFragmentOffset()).compareTo(
						Integer.valueOf(o2.getFragmentOffset()));
			}
		});
	
	public synchronized void addFragment(IPFragment packet) {
		fragments.add(packet);
	}
	
	public synchronized boolean isComplete() {
		if (fragments.first().getFragmentOffset() != 0) {
			return false;
		}
		if (fragments.last().hasMoreFragments()) {
			return false;
		}
		int lastOffset = 0;
		for (IPFragment packet: fragments) {
			int offset = packet.getFragmentOffset();
			if (offset > lastOffset) {
				return false;
			}
			lastOffset = offset + packet.getLength(); // XXX lentgh()?
		}
		return true;
	}
	
	private int reassembledSize() {
		IPFragment lastFragment = fragments.last();
		return lastFragment.getFragmentOffset() + lastFragment.getLength();
	}
	
	public synchronized IP getReassembled() {
		ByteBuffer buffer = ByteBuffer.allocate(reassembledSize());
		buffer.put(fragments.first().toByteBuffer());
		for (IPFragment fragment: fragments) {
			buffer.position(fragment.getFragmentOffset());
			buffer.put(fragment.payload().toByteBuffer());
		}
		buffer.flip();
		IP answer = fragments.first().createPacket();
		answer.unpack(buffer);
		answer.setTotalLength(answer.getTotalLength() + buffer.limit()
				- fragments.first().getLength());
		return answer;
	}
}
