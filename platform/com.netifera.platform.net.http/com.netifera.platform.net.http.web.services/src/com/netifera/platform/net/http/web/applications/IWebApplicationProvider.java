package com.netifera.platform.net.http.web.applications;

import java.net.URI;

import com.netifera.platform.net.http.service.HTTP;

public interface IWebApplicationProvider {
	String getServiceName();
	Class<? extends IWebApplication> getServiceClass();
	IWebApplication create(HTTP transport, URI url);
}
