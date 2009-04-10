package com.netifera.platform.net.internal.services.detection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.net.services.detection.INetworkServiceDetectorProvider;
import com.netifera.platform.net.services.detection.INetworkServiceTrigger;
import com.netifera.platform.net.services.detection.IServerDetectorService;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.patternmatching.IPattern;


public class ServerDetectorService extends NetworkServiceDetectorService implements IServerDetectorService {
	private final ArrayList<INetworkServiceTrigger> triggers = new ArrayList<INetworkServiceTrigger>();

	public byte[] getTrigger(String protocol, int port) {
		for (INetworkServiceTrigger trigger: triggers)
			if (trigger.getProtocol().equals(protocol) && trigger.getPorts().contains(port)) {
				return trigger.getBytes();
			}
		return new byte[0];
	}
	
	private final static PortSet DEFAULT_TCP_PORTSET = new PortSet("21-23,25,80,110,111,143,443,445,1433,1521,3306,8000,8080,8081,8888");
	private final static PortSet DEFAULT_UDP_PORTSET = new PortSet("53,111,137,139,161,5060");
	
	// Returns the PortSet of (pre-registered) triggerable ports for a given protocol
	public PortSet getTriggerablePorts(String protocol) {
		if (protocol.equals("tcp")) {
			return DEFAULT_TCP_PORTSET;
		}
		return DEFAULT_UDP_PORTSET;
		/*
		int MAX_PORTSET_SIZE = 10; // FIXME
		PortSet answer = new PortSet();
		for (INetworkServiceTrigger trigger: triggers) {
			if (trigger.getProtocol().equals(protocol)) {
				PortSet triggerPorts = trigger.getPorts();
				//if (triggerPorts.itemCount() < MAX_PORTSET_SIZE) {
					answer.addPortSet(triggerPorts);
				//}
			}
		}
		return answer;
		*/
	}

	protected void registerDetectorProvider(INetworkServiceDetectorProvider provider) {
		for (INetworkServiceTrigger each: provider.getTriggers())
			triggers.add(each);
		
		for (INetworkServiceDetector each: provider.getServerDetectors())
			addDetector(each);
	}

	protected void unregisterDetectorProvider(INetworkServiceDetectorProvider provider) {
		// epa!
	}
	
	public Map<String,String> detect(String protocol, int port,
			String trigger, String response) {
		List<INetworkServiceDetector> detectors = this.detectors.get(protocol);
		if (detectors == null)
			return null;
		for (INetworkServiceDetector each: detectors) {
			if (each.getPorts() != null && !each.getPorts().contains(port)) continue;
			Map<String,String> result = each.detect(trigger, response);
			if (result != null) {
				if (result.get("serviceType").equals("HTTP"))
					response = response.split("\\n\\n|\\r\\n\\r\\n")[0];
				if (!result.containsKey("banner") && isPrintable(response))
					result.put("banner", response);
				String os = result.get("os");
				if (os == null || os.equals("Unix"))
					for (IPattern osDetector: genericOSDetectors)
						if (osDetector.match(result, response))
							return result;
				return result;
			}
		}
		return null;
	}
}
