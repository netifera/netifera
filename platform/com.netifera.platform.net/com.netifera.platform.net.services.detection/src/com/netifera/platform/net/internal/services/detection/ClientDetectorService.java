package com.netifera.platform.net.internal.services.detection;

import java.util.List;
import java.util.Map;

import com.netifera.platform.net.services.detection.IClientDetectorService;
import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.net.services.detection.INetworkServiceDetectorProvider;
import com.netifera.platform.util.patternmatching.IPattern;

public class ClientDetectorService extends NetworkServiceDetectorService implements IClientDetectorService {
	protected void registerDetectorProvider(INetworkServiceDetectorProvider provider) {
		for (INetworkServiceDetector each: provider.getClientDetectors())
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
				if (!result.containsKey("banner") && isPrintable(trigger))
					result.put("banner", trigger);
				String os = result.get("os");
				if (os == null || os.equals("Unix"))
					for (IPattern osDetector: genericOSDetectors)
						if (osDetector.match(result, trigger))
							break;
				if (result.get("arch") == null)
					for (IPattern archDetector: genericArchDetectors)
						if (archDetector.match(result, trigger))
							break;
				return result;
			}
		}
		return null;
	}
}
