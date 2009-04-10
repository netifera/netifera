package com.netifera.platform.net.dns.internal.sniffing;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.net.dns.model.IDomainEntityFactory;
import com.netifera.platform.net.model.INetworkEntityFactory;


public class Activator implements BundleActivator {
	private ServiceTracker probeManagerTracker;
	private ServiceTracker networkEntityFactoryTracker;
	private ServiceTracker domainEntityFactoryTracker;

	private static Activator _instance;
	
	public static Activator getInstance() {
		return _instance;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		_instance = this;

		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();

		networkEntityFactoryTracker = new ServiceTracker(context, INetworkEntityFactory.class.getName(), null);
		networkEntityFactoryTracker.open();

		domainEntityFactoryTracker = new ServiceTracker(context, IDomainEntityFactory.class.getName(), null);
		domainEntityFactoryTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		_instance = null;
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public INetworkEntityFactory getNetworkEntityFactory() {
		return (INetworkEntityFactory) networkEntityFactoryTracker.getService();
	}
	
	public IDomainEntityFactory getDomainEntityFactory() {
		return (IDomainEntityFactory) domainEntityFactoryTracker.getService();
	}
}
