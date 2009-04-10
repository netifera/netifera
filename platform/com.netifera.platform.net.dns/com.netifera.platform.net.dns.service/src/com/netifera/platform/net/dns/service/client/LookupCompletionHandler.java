package com.netifera.platform.net.dns.service.client;

public interface LookupCompletionHandler {
	void completed(AsynchronousLookup lookup);
}
