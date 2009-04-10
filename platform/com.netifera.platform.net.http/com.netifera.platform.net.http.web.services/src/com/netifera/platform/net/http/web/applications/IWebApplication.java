package com.netifera.platform.net.http.web.applications;

import java.net.URI;

import com.netifera.platform.net.http.service.HTTP;

public interface IWebApplication {
	HTTP getHTTP();
	URI getURL();
}
