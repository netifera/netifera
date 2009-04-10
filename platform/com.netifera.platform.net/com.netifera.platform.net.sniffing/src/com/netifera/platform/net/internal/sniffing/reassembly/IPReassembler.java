package com.netifera.platform.net.internal.sniffing.reassembly;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import com.netifera.platform.net.packets.tcpip.IP;

public class IPReassembler {
	final private ConcurrentMap<IPReassemblyKey, IPReassemblyQueue> reassemblyQueues = new ConcurrentHashMap<IPReassemblyKey, IPReassemblyQueue>();
	final private ConcurrentLinkedQueue<IP> outputQueue = new ConcurrentLinkedQueue<IP>();
	
	public void addPacket(IP packet) {
		if (packet.isFragment()) {
			addFragment(packet);
		} else {
			outputQueue.offer(packet);
		}
	}
	
	private void addFragment(IP packet) {
		IPReassemblyKey key = new IPReassemblyKey(packet);
		IPReassemblyQueue queue = reassemblyQueues.get(key);
		if (queue == null) {
			queue = new IPReassemblyQueue();
			reassemblyQueues.put(key,queue);
		}
		queue.addFragment(packet.fragment());
		if (queue.isComplete()) {
			reassemblyQueues.remove(key);
			outputQueue.offer(queue.getReassembled());
		}
	}
	
	public int count() {
		return outputQueue.size();
	}
	
	public IP getNextPacket() {
		return outputQueue.poll(); // XXX not blocking?
	}
}
