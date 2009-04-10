package com.netifera.platform.net.internal.services.detection.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.netifera.platform.net.services.detection.INetworkServiceDetector;
import com.netifera.platform.util.PortSet;

public class OracleDetector implements INetworkServiceDetector {
	private final Pattern tnsAcceptPattern = Pattern.compile("(?s-m)" +
			"^.{4}" + // length: 2bytes + checksum: 2bytes
			"\\x02" + // TNS ACCEPT packet type
			".*\\(DESCRIPTION=.*\\(VSN?NUM=(\\d+)\\).*\\(ERR=\\d+\\).*");
	
	public Map<String, String> detect(String trigger, String response) {
		Matcher acceptMatcher = tnsAcceptPattern.matcher(response);
		if (!acceptMatcher.matches()) {
			return null;
		}
		
		Map<String, String> answer = new HashMap<String, String>();
		answer.put("serviceType", "Oracle");
		
		/* Document: Release Notes for Oracle Database
		 * 
		 * Starting with the 7.0.0.0.0 release, the product version number is
		 * comprised of a 5-part number as follows:
		 * 
		 * TimesTen Product Version = Major.Minor.Patch.Dot4.Dot5
		 * 
		 * - Major (8bits)
		 *   specifies a major release version where the release contains both
		 *   infrastructure and functionality changes in the product.
		 * - Minor (4bits)
		 *   specifies a minor release version where new significant feature
		 *   are added to the product without major infrastructure changes.
		 * - Patch (8bits)
		 *   specifies a maintenance release that rolls up all bug fixes since
		 *   the previous maintenance release.
		 * - Dot4 (4bits)
		 *   specifies the vsersion for a bug fix release that cannot wait for
		 *   the next scheduled Patch release.
		 * - Dot5 (8bits)
		 *   is reserved for special cases.
		 */
		long version = Long.parseLong(acceptMatcher.group(1)); // in decimal
		answer.put("version", (version >> 24) + "." + ((version >> 20) & 0xf)
				+ "." + ((version >> 12) & 0xff) + "." + ((version >> 8) & 0xf)
				+ "." + (version & 0xff));
		
		int listenerDescriptionIndex = response.indexOf("TNSLSNR");
		if (listenerDescriptionIndex != -1) {
			answer.put("banner", response.substring(listenerDescriptionIndex)
					.split("[^\\p{Print}\\p{Blank}]")[0]);
		}
		
		return answer;
	}
	
	public PortSet getPorts() {
		return null;
	}
	
	public String getProtocol() {
		return "tcp";
	}
}
