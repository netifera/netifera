package com.netifera.platform.net.tools.basic;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.internal.tools.portscanning.Activator;
import com.netifera.platform.net.model.NetblockEntity;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.addresses.inet.InternetNetblock;

public class AddNetblock implements ITool {
	
	private IToolContext context;
	private long realm;
	private InternetNetblock netblock;

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();
		
		setupToolOptions();

		context.setTitle("Add netblock "+netblock);
		NetblockEntity entity = Activator.getInstance().getNetworkEntityFactory().createNetblock(realm, context.getSpaceId(), netblock);
		entity.addTag("Target");
		entity.update();
		context.info("Netblock "+netblock+" added to the model");
	}
	
	private void setupToolOptions() throws RequiredOptionMissingException {
		netblock = (InternetNetblock) context.getConfiguration().get("netblock");
		if(netblock == null) {
			throw new RequiredOptionMissingException("netblock");
		}
	}
}
