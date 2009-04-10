package com.netifera.platform.net.services.sniffing;

import java.nio.ByteBuffer;

import com.netifera.platform.net.services.credentials.Credential;


public interface ICredentialSnifferService {
	Credential sniff(String serviceType, String clientData, String serverData);
	Credential sniff(String serviceType, ByteBuffer clientData, ByteBuffer serverData);
}
