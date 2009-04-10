package com.netifera.platform.net.internal.daemon.sniffing.modules;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.services.detection.IClientDetectorService;
import com.netifera.platform.net.services.detection.IServerDetectorService;
import com.netifera.platform.net.services.sniffing.ICredentialSnifferService;

public class Activator implements BundleActivator {
	private ServiceTracker networkEntityFactoryTracker;
	private ServiceTracker probeManagerTracker;
	private ServiceTracker serverDetectorTracker;
	private ServiceTracker clientDetectorTracker;
	private ServiceTracker credentialSnifferTracker;

	private static Activator instance;
	
	public static Activator getInstance() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		instance = this;
		networkEntityFactoryTracker = new ServiceTracker(context, INetworkEntityFactory.class.getName(), null);
		networkEntityFactoryTracker.open();
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		serverDetectorTracker = new ServiceTracker(context, IServerDetectorService.class.getName(), null);
		serverDetectorTracker.open();

		clientDetectorTracker = new ServiceTracker(context, IClientDetectorService.class.getName(), null);
		clientDetectorTracker.open();
		
		credentialSnifferTracker = new ServiceTracker(context, ICredentialSnifferService.class.getName(), null);
		credentialSnifferTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		instance = null;
	}
	
	public INetworkEntityFactory getNetworkEntityFactory() {
		return (INetworkEntityFactory) networkEntityFactoryTracker.getService();
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}

	public IServerDetectorService getServerDetector() {
		return (IServerDetectorService) serverDetectorTracker.getService();
	}

	public IClientDetectorService getClientDetector() {
		return (IClientDetectorService) clientDetectorTracker.getService();
	}
	
	public ICredentialSnifferService getCredentialSniffer() {
		return (ICredentialSnifferService) credentialSnifferTracker.getService();
	}
}
