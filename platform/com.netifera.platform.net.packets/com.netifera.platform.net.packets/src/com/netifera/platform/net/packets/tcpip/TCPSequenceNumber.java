package com.netifera.platform.net.packets.tcpip;

public class TCPSequenceNumber implements Comparable<TCPSequenceNumber> {
	private final int value;

	public TCPSequenceNumber(int value) {
		this.value = value;
	}

	public int toInteger() {
		return value;
	}

	@Override
	public int hashCode() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TCPSequenceNumber) {
			return value == ((TCPSequenceNumber) o).value;
		}
		return false;
	}
	
	/**
	 * Return true if this sequence number is greater than the
	 * sequence number specified as an argument.
	 * 
	 * @param another Sequence number to compare.
	 * @return True if this sequence number is greater than argument.
	 */
	public boolean greater(TCPSequenceNumber another) {
		return compareTo(another) > 0;
	}
	
	/**
	 * Return true if this sequence number is less than the
	 * sequence number specified as an argument.
	 * 
	 * @param another Sequence number to compare.
	 * @return True if this sequence number is less than argument.
	 */
	public boolean less(TCPSequenceNumber another) {
		return compareTo(another) < 0;
	}
	
	public boolean greaterOrEqual(TCPSequenceNumber another) {
		return compareTo(another) >= 0;
	}
	
	public boolean lessOrEqual(TCPSequenceNumber another) {
		return compareTo(another) <= 0;
	}
	
	
	public int compareTo(TCPSequenceNumber another) {
		return (-distance(this.value, another.value));
	}

	public int distanceTo(TCPSequenceNumber another) {
		return (distance(this.value, another.value));
	}

	private int distance(int a, int b) {
		/* if the values sign is different then add 2**30 to both, add nothing otherwise*/
		int shift = ((a >> 1 & 0x40000000)) ^ ((b >> 1 & 0x40000000));
		return (b + shift) - (a + shift);

	}

	public TCPSequenceNumber add(int n) {
		return new TCPSequenceNumber(value + n);
	}

	public TCPSequenceNumber next() {
		return add(1);
	}

	public TCPSequenceNumber previous() {
		return add(-1);
	}

	@Override
	public String toString() {
		return Long.toString(value & 0xffffffffL);
	}
}
