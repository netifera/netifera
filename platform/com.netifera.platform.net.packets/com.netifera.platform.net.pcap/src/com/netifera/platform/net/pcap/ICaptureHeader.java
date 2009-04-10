package com.netifera.platform.net.pcap;

public interface ICaptureHeader {
	/**
	 * @return Capture time seconds.
	 */
	long getSeconds();
	/**
	 * @return Capture time microseconds.
	 */
	int getMicroseconds();
	/**
	 * @return Length of captured data in bytes.
	 */
	int getCaplen();
	/**
	 * @return Length of original packet in bytes.
	 */
	int getDatalen();

}
