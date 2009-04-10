package com.netifera.platform.net.http.web.model;

import java.net.URI;
import java.util.Map;

import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;

public interface IWebEntityFactory {
	ServiceEntity createWebServer(final long realm, long space, TCPSocketLocator http, String product);
	WebSiteEntity createWebSite(long realm, long space, TCPSocketLocator http, String hostname);
	void setFavicon(long realm, long space, TCPSocketLocator http, URI url, byte[] faviconBytes);
	WebPageEntity createWebPage(long realm, long space, TCPSocketLocator http, URI url, String contentType);
	WebApplicationEntity createWebApplication(long realm, long space, TCPSocketLocator http, URI url, Map<String,String> info);
	
	HTTPBasicAuthenticationEntity createBasicAuthentication(long realm, long space, TCPSocketLocator http, URI url, String authenticationRealm);
	WebFormAuthenticationEntity createFormAuthentication(long realm, long space, TCPSocketLocator http, URI url, String usernameField, String passwordField);
	
	HTTPRequestEntity createRequestResponse(long realm, long space, InternetAddress clientAddress, Map<String,String> clientInfo, TCPSocketLocator service, String requestLine, String responseStatusLine, String contentType);
}
