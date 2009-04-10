package com.netifera.platform.net.services.sniffing;

import java.util.Map;

import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.Password;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.patternmatching.Regex;
import com.netifera.platform.util.patternmatching.SessionPattern;

public class RegexCredentialSniffer implements ICredentialSniffer {
	private final String[] serviceTypes;
	private final SessionPattern pattern;
	
	public RegexCredentialSniffer(String[] serviceTypes, Regex clientPattern, Regex serverPattern) {
		this.serviceTypes = serviceTypes.clone(); // FIXME do we really need to clone here?
		this.pattern = new SessionPattern(clientPattern, serverPattern);
	}

	public Credential sniff(String clientData, String serverData) {
		Map<String,String> result = pattern.match(clientData, serverData);
		if (result == null) return null;
		
		if (result.containsKey("password"))
			if (result.containsKey("username"))
				return new UsernameAndPassword(result.get("username"), result.get("password"));
			else
				return new Password(result.get("password"));
//		else
//			if (result.contains("username"))
//				return new Username(result.get("username"));
		return null;
	}
	
	public String[] getServiceTypes() {
		return serviceTypes;
	}
}
