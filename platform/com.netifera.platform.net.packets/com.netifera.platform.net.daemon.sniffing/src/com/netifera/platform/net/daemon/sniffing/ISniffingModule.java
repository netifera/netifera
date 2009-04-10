package com.netifera.platform.net.daemon.sniffing;

import com.netifera.platform.net.sniffing.IPacketFilter;

/**
 * This interface is the base for all of the extensions to the packet sniffing
 * daemon.  This interface should not be directly implemented but rather one or
 * more of the following derived interfaces should be implemented to create a
 * sniffing daemon module:
 * <ul>
 *   <li> <code>IArpSniffer</code> </li>
 *   <li> <code>IIPSniffer</code> </li>
 *   <li> <code>IRawSniffer</code> </li>
 *   <li> <code>ITCPBlockSniffer</code> </li>
 *   <li> <code>ITCPStreamSniffer</code> </li>
 * </ul>
 * 
 * The module will receive packets or assembled TCP stream data corresponding to
 * the implemented sniffing module interface(s).
 * 
 * @see IArpSniffer
 * @see IIPSniffer
 * @see IRawSniffer
 * @see ITCPBlockSniffer
 * @see ITCPStreamSniffer
 *
 */
public interface ISniffingModule {
	
	/**
	 * Return a <code>String</code> which describes this sniffing module. This
	 * is the name which will appear in the configuration panel of the sniffing
	 * service in the UI.
	 * 
	 * @return String description of this module.
	 */
	String getName();
	
	/**
	 * Return a <code>IPacketFilter</code> instance which restricts the packets
	 * received by this module. Packet filtering is currently not implemented in
	 * netifera.
	 * 
	 * <p>Sniffing module implementations should always return <code>null</code>
	 * from this method.</p>
	 * 
	 * @return Packet filter to be applied for this module.
	 */
	IPacketFilter getFilter();
}
