package com.netifera.platform.net.daemon.sniffing;

import java.util.Collection;
import java.util.Set;

import com.netifera.platform.api.daemon.IDaemon;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;

/**
 * 
 *
 *
 */
public interface ISniffingDaemon extends IDaemon {
	/**
	 * Return a set of available sniffing modules.
	 * 
	 * @return A set containing all available sniffing modules.
	 */
	Set<ISniffingModule> getModules();
	
	/**
	 * Return a collection of available network interfaces which 
	 * support packet capture.
	 * 
	 * @return A collection containing all available network interfaces.
	 */
	Collection<ICaptureInterface> getInterfaces();
	

	/**
	 * Tests the specified interface and returns true if it is enabled.
	 * 
	 * @param iface The interface to test.
	 * @return <tt>true</tt> if the interface is currently enabled.
	 */
	boolean isEnabled(ICaptureInterface iface);

	/**
	 * Enable or disable a single interface.  The specified interface
	 * will be enabled or disabled depending on the value of the <code>
	 * enable</code> flag.
	 * 
	 * @param iface  The interface to enable or disable.
	 * @param enable If <tt>true</tt> enable the interface, otherwise disable it.
	 */
	void setEnabled(ICaptureInterface iface, boolean enable);
	
	
	
	/**
	 * Tests the specified sniffing module and returns true if it is enabled.
	 * 
	 * @param module The module to test.
	 * @return <tt>true</tt> if the module is currently enabled.
	 */
	boolean isEnabled(ISniffingModule module);
	
	/**
	 * Enable or disable a single sniffing module.  The specified module will
	 * be enabled or disabled depending on the value of the <code>enable</code>
	 * flag.
	 * 
	 * @param module The sniffing module to enable or disable.
	 * @param enable If <tt>true</tt> enable the module, otherwise disable it.
	 */
	void setEnabled(ISniffingModule module, boolean enable);
	
	/**
	 * Configure the sniffing daemon with a set of sniffing modules which
	 * should be enabled once the daemon is started.  
	 * 
	 * It is an error to call this method while the sniffing daemon is running.  
	 * Clients should call {@link IDaemon#isRunning()} before invoking this method.
	 * 
	 * The set of modules to enable must not be empty.
	 * 
	 * @param enabledModuleSet Set of sniffing modules to enable.
	 * @throws IllegalStateException Method called while daemon is running.
	 * @throws IllegalArgumentException Method called with empty set of modules, or
	 * contains module instances which were not returned by {@link #getModules()}
	 */
	void enableModules(Set<ISniffingModule> enabledModuleSet);
	
	/**
	 * Configure the sniffing daemon with a collection of network interfaces
	 * which should be used once the daemon is started.  
	 * 
	 * It is an error to call this method while the sniffing daemon is running.  
	 * Clients should call {@link IDaemon#isRunning()} before invoking this method.
	 * 
	 * The set of interfaces to enable must not be empty.
	 * 
	 * @param interfaces Network interfaces to enable for capture.
	 * @throws IllegalStateException Method called while daemon is running.
	 * @throws IllegalArgumentException Method called with empty set of interfaces, or
	 * contains interface instances which were not returned by {@link #getInterfaces()}
	 */
	void enableInterfaces(Collection<ICaptureInterface> interfaces);

	/**
	 * Create and return a capture file interface for the given path.
	 * 
	 * @param path Path to the pcap capture file to use to create the interface.
	 * @return Newly created capture file interface.
	 */
	ICaptureFileInterface createCaptureFileInterface(String path);
	
	/**
	 * Process a packet capture file in a background thread and report progress with
	 * the <code>progress</code> callback.
	 * 
	 * @param iface
	 * @param progress
	 */
	void runCaptureFile(long spaceId, ICaptureFileInterface iface, ICaptureFileProgress progress);
	
	void cancelCaptureFile();
}
