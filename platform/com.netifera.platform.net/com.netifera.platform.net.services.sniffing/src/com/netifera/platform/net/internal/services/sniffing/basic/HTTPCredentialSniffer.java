package com.netifera.platform.net.internal.services.sniffing.basic;

import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.net.services.sniffing.ICredentialSniffer;
import com.netifera.platform.util.Base64;

public class HTTPCredentialSniffer implements ICredentialSniffer {
	public Credential sniff(String clientData, String serverData) {
		String authToken = "Authorization: ";
		String encodedAuth;
		String scheme;
		String username;
		String password;
		int i;
		int j;
		byte[] decodedBytes;
		
		if (clientData == null) {
			return null;
		}
		
		i = clientData.indexOf(authToken);
		if (i == -1) {
			return null;
		}
			
		i += authToken.length();
			
		j = clientData.indexOf(' ', i);
		if (j == -1) {
			return null;
		}
			
		scheme = clientData.substring(i, j);
		
		if (scheme.compareTo("Basic") != 0)
			return null;
			
		i = j + 1;
			
		j = clientData.indexOf('\n', i);
		if (j == -1) {
			return null;
		}

		encodedAuth = clientData.substring(i);
		decodedBytes =  Base64.decode(encodedAuth);
		
		if(decodedBytes == null) {
			return null;
		}
		
		String decodedAuth = new String(decodedBytes);
			
		String[] authParams =  decodedAuth.split(":");
		if (authParams.length != 2)
			return null;
			
		username = authParams[0];
		password = authParams[1];
			
		//System.out.println("Authentication scheme: " + scheme + ", username: " + username + ", password: " + password);
		return new UsernameAndPassword(username, password);
	}

	public String[] getServiceTypes() {
		return new String[] {"HTTP"};
	}
}
