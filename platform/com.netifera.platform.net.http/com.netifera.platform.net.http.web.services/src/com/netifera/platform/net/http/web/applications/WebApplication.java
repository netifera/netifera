package com.netifera.platform.net.http.web.applications;

import java.io.Serializable;
import java.net.URI;

import com.netifera.platform.net.http.service.HTTP;


public abstract class WebApplication implements IWebApplication, Serializable {
	private static final long serialVersionUID = -5155689013060101376L;
	
	final private HTTP http;
	final private URI url;

	public WebApplication(HTTP http, URI url) {
		this.http = http;
		this.url = url;
	}
	
	public HTTP getHTTP() {
		return http;
	}

	public URI getURL() {
		return url;
	}
}
