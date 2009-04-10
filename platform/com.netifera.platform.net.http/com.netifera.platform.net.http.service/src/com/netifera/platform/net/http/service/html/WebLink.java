package com.netifera.platform.net.http.service.html;

import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.message.BasicHttpRequest;

public class WebLink {
	final private URI url;
	
	public WebLink(URI url) {
		this.url = url;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null || !(obj instanceof WebLink)) {
			return false;
		}
		return url.equals(((WebLink)obj).url);
	}
	
	@Override
	public int hashCode() {
		return url.hashCode();
	}
	
	public URI url() {
		return url;
	}
	
	public String method() {
		return "GET";
	}

	public HttpRequest request() {
		HttpRequest answer = new BasicHttpRequest(method(), url.getPath());
		if (url.getHost() != null)
			answer.setHeader("Host", url.getHost());
		return answer;
	}
}
