package com.netifera.platform.net.services.sniffing;

import com.netifera.platform.net.services.credentials.Credential;


public interface ICredentialSniffer {
	String[] getServiceTypes();
	Credential sniff(String clientData, String serverData);
}
