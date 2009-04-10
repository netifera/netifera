package com.netifera.platform.net.tools.portscanning;


import java.util.concurrent.atomic.AtomicInteger;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.internal.tools.portscanning.Activator;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public abstract class AbstractPortscanner implements ITool {

	protected IToolContext context;
	protected IndexedIterable<InternetAddress> targetNetwork;
	protected PortSet targetPorts;
	final private AtomicInteger outstandingConnects = new AtomicInteger(0);
	protected long realm;

	public void toolRun(IToolContext context) throws ToolException {
		assert(context != null);
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();

		setupPortscannerOptions();
		//task.setTotalWork(targetNetwork.itemCount() * targetPorts.itemCount());
		try {
			scannerRun();
		} finally {
			context.done();
		}
	}
	
	protected abstract void scannerRun() throws ToolException;
	
	protected void setupToolOptions() throws ToolException {
		/* Override me for handling tool specific options */
	}
	
	private void setupPortscannerOptions() throws ToolException {
		setupToolOptions();

		targetNetwork = (IndexedIterable<InternetAddress>) context.getConfiguration().get("target");
		if(targetNetwork == null) {
			throw new RequiredOptionMissingException("target");
		}

		String portsString = (String)context.getConfiguration().get("ports");
		if(portsString == null) {
			throw new RequiredOptionMissingException("ports");
		}
		try {
			targetPorts = new PortSet(portsString);
		} catch (IllegalArgumentException e) {
			throw new ToolException("Invalid ports: "+portsString);
		}
	}
	
	void incrementOutstanding() {
		outstandingConnects.incrementAndGet();
	}
	
	void decrementOutstanding() {
		outstandingConnects.decrementAndGet();
	}
	
	int getOutstandingCount() {
		return outstandingConnects.get();
	}
}