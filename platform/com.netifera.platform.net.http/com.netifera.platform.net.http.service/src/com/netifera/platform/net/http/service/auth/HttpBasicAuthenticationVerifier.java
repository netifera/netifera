package com.netifera.platform.net.http.service.auth;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.protocol.HttpRequestExecutionHandler;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.protocol.HttpContext;

import com.netifera.platform.net.http.service.AsynchronousHTTPClient;
import com.netifera.platform.net.http.service.HTTP;
import com.netifera.platform.net.services.auth.AuthenticationListener;
import com.netifera.platform.net.services.auth.CredentialsVerifier;
import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.util.Base64;

public class HttpBasicAuthenticationVerifier extends CredentialsVerifier {
	private final HTTP service;
	private final URI url;

	public HttpBasicAuthenticationVerifier(HTTP service, URI url) {
		this.service = service;
		this.url = url;
	}
	
	private class CredentialsTester implements HttpRequestExecutionHandler {
		public void initalizeContext(HttpContext context, Object arg) {
			// TODO Auto-generated method stub
			
		}

		public void finalizeContext(HttpContext context) {
			// TODO Auto-generated method stub
			
		}

		public HttpRequest submitRequest(HttpContext context) {
			Credential credential = nextCredentialOrNull();
//			System.out.println("submitRequest "+credential);
			if (credential == null) return null;
			context.setAttribute("credential", credential);
			HttpRequest request = new BasicHttpRequest("GET", url.getRawPath());
//			HttpRequest request = new BasicHttpRequest("GET", url.getRawPath()+"?"+url.getRawQuery());
			request.addHeader("Host", url.getHost());
			request.addHeader("Authorization", "Basic "+base64((UsernameAndPassword)credential));
//			System.out.println(credential+" request: "+request.getRequestLine());
			return request;
		}
		
		public void handleResponse(HttpResponse response, HttpContext context)
				throws IOException {
			Credential credential = (Credential) context.getAttribute("credential");
//			System.out.println(credential+" response: "+response.getStatusLine());
			if (response.getStatusLine().getStatusCode() == 401) {
				listener.authenticationFailed(credential);
			} else {
				listener.authenticationSucceeded(credential);
			}
		}
	}
	
	@Override
	public void tryCredentials(Iterator<Credential> credentials,
			AuthenticationListener listener) throws IOException,
			InterruptedException {

		this.credentials = credentials;
		this.listener = listener;
		
		AsynchronousHTTPClient client = service.createAsynchronousClient(new CredentialsTester());
		client.connect(new SessionRequestCallback() {
			// XXX never return on error
			
			public void cancelled(SessionRequest arg0) {
				// TODO Auto-generated method stub
			}

			public void completed(SessionRequest arg0) {
				// TODO Auto-generated method stub
			}

			public void failed(SessionRequest arg0) {
				// TODO Auto-generated method stub
			}

			public void timeout(SessionRequest arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	// helper method
	private String base64(UsernameAndPassword credential) {
		String userAndPassString = credential.getUsernameString()+":"+credential.getPasswordString();
		return Base64.encodeBytes(userAndPassString.getBytes());
	}
}
