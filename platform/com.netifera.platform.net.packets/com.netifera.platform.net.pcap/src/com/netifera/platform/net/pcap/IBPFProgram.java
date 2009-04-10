package com.netifera.platform.net.pcap;

public interface IBPFProgram {
	
	/* instruction classes */
	int BPF_ALU 	= 0x04;
	int BPF_JMP 	= 0x05;
	int BPF_RET 	= 0x06;
	
	/* alu/jmp fields */
	int BPF_K 		= 0x00;
	int BPF_X 		= 0x08;
	int BPF_A 		= 0x10;
	
	/**
	 * Add a BPF Instructions to the BPF program.
	 * 
	 * @param code BPF Instruction code (16bits)
	 * @param jt Jump to that block if <code>true</code>
	 * @param jf Jump to that block if <code>false</code>
	 * @param k A generic BPF multiuse field
	 */
	void addInstruction(int code, int jt, int jf, int k);
	
	/**
	 * The instructions array.
	 * 
	 * @return The instructions array
	 */
	byte[] getBytes();
	
	/**
	 * Total number of instructions in the BPF program.
	 * 
	 * @return The total number of instructions in the BPF program
	 */
	int getInstructionCount();
}
