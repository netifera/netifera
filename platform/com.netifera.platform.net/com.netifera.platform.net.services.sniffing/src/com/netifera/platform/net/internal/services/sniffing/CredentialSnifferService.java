package com.netifera.platform.net.internal.services.sniffing;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.sniffing.ICredentialSniffer;
import com.netifera.platform.net.services.sniffing.ICredentialSnifferProvider;
import com.netifera.platform.net.services.sniffing.ICredentialSnifferService;

public class CredentialSnifferService implements ICredentialSnifferService {
	private final Map<String, ICredentialSniffer> sniffers = new HashMap<String, ICredentialSniffer>();
	
	protected void registerCredentialSniffers(ICredentialSnifferProvider provider) {
		for (ICredentialSniffer sniffer: provider.getSniffers())
			for (String serviceType: sniffer.getServiceTypes()) {
				sniffers.put(serviceType, sniffer);
			}
	}

	protected void unregisterCredentialSniffers(ICredentialSnifferProvider provider) {
		// TODO
	}
	
	public Credential sniff(String serviceType, String clientData, String serverData) {
		ICredentialSniffer sniffer = sniffers.get(serviceType);
		if (sniffer == null)
			return null;
		
		return sniffer.sniff(clientData, serverData);
	}
	
	public Credential sniff(String serviceType, ByteBuffer clientBuffer, ByteBuffer serverBuffer) {
		String clientString;
		String serverString;
		if (clientBuffer != null && clientBuffer.remaining() > 0) {
			clientString = stringFromByteBuffer(clientBuffer);
		} else {
			clientString = "";
		}
		if (serverBuffer != null && serverBuffer.remaining() > 0) {
			serverString = stringFromByteBuffer(serverBuffer);
		} else {
			serverString = "";
		}
		if (clientString.length() == 0 && serverString.length() == 0) {
			return null;
		}
		return sniff(serviceType, clientString, serverString);
	}

	private String stringFromByteBuffer(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		try {
			return new String(bytes, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(); // XXX
			throw new RuntimeException("Unsupported encoding: ISO-8859-1");
		}
	}
}