package com.netifera.platform.net.internal.pcap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.netifera.platform.net.pcap.IBPFProgram;

public class BPFProgram implements IBPFProgram {
	private static int BPF_MAXINSNS = 512;
	private static int BUFFER_SIZE = BPF_MAXINSNS * 8;
	private final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
	private int instructionCount;
	
	public BPFProgram() {
		buffer.order(ByteOrder.nativeOrder());
	}
	public void addInstruction(int code, int jt, int jf, int k) {
		buffer.putShort((short) code);
		buffer.put((byte) jt);
		buffer.put((byte) jf);
		buffer.putInt(k);
		instructionCount++;
	}
	
	public int getInstructionCount() {
		return instructionCount;
	}
	public byte[] getBytes() {
		int instructionLength = buffer.position();
		byte[] data = new byte[instructionLength];
		System.arraycopy(buffer.array(), 0, data, 0, instructionLength);
		return data;
	}
	
	public boolean filterPacket(byte[] data, int datalen, int caplen) {
		throw new RuntimeException("filter packet not implemented");
	}
	
	

}
