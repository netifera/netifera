package com.netifera.platform.net.internal.sniffing.file;

import java.nio.ByteBuffer;

import com.netifera.platform.net.pcap.ICaptureHeader;

public interface ICaptureFileRecord extends ICaptureHeader {
	ByteBuffer getRecordBytes();
}
