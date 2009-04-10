package com.netifera.platform.net.internal.pcap;

import java.util.List;

import com.netifera.platform.net.pcap.Datalink;
import com.netifera.platform.net.pcap.IBPFProgram;

/**
 * This interface provides access to methods of <code>PacketCapture</code> that are only
 * meant to be used by native packet capture providers
 * @author brl
 *
 */
public interface IPacketCaptureInternal {
	void setError(String error);
	String getLastError();
	IBPFProgram createBPFProgram();
	void dltListClear();
	void dltListAdd(Datalink dlt);
	List<Datalink> getDltList();
	Datalink dltLookup(int n);
	String getInterfaceName();
}
