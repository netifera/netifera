package com.netifera.platform.demo.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.net.services.detection.INetworkServiceDetectorProvider;
import com.netifera.platform.net.services.detection.INetworkServiceTrigger;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.patternmatching.ISessionPattern;
import com.netifera.platform.util.patternmatching.Regex;
import com.netifera.platform.util.patternmatching.SessionPattern;

public class ServiceDetectorProvider implements INetworkServiceDetectorProvider {
	
	private INetworkServiceDetector newDetector(final String protocol, final PortSet ports, final ISessionPattern pattern) {
		
		return new INetworkServiceDetector() {
			public Map<String, String> detect(String clientData,
					String serverData) {
				return pattern.match(clientData, serverData);
			}
			public PortSet getPorts() {
				return ports;
			}
			public String getProtocol() {
				return protocol;
			}
			@Override
			public String toString() {
				return ports.toString() + "/" + protocol + "\n" + pattern.toString();
			}
		};
	}

	private INetworkServiceDetector newDetector(String protocol, PortSet ports, String triggerPattern, String responsePattern, String serviceType, String product, Object versionGroup, Object os) {
		Regex triggerRegex = new Regex(triggerPattern);
		Regex responseRegex = new Regex(responsePattern);
		responseRegex.add("serviceType", serviceType);
		responseRegex.add("product", product);
		if (os instanceof String) {
			responseRegex.add("os", (String)os);
			if (((String) os).matches(".*indow.*"))
					responseRegex.add("arch", "i386"); // FIXME x86_64
		} else if (os instanceof Integer) {
			responseRegex.add((Integer)os, "os");
		}
		if (versionGroup != null) {
			if (versionGroup instanceof String) {
				responseRegex.add("version", (String)versionGroup);
			} else if (versionGroup instanceof Integer) {
				responseRegex.add((Integer)versionGroup, "version");
			}
		}
		return newDetector(protocol,ports,new SessionPattern(triggerRegex, responseRegex));
	}

	public List<INetworkServiceDetector> getClientDetectors() {
		return Collections.emptyList();
	}

	@SuppressWarnings("boxing")
	public List<INetworkServiceDetector> getServerDetectors() {
		List<INetworkServiceDetector> answer = new ArrayList<INetworkServiceDetector>();
		
		answer.add(newDetector("tcp", new PortSet("1234"),
				".*", "Netifera Test Service.*", "TEST", "Netifera Test Service", null, null));

		return answer;
	}

	public List<INetworkServiceTrigger> getTriggers() {
		return Collections.emptyList();
	}
}
