package com.netifera.platform.net.sockets;

public interface CompletionHandler<V,A> {
	void cancelled(A attachment);
	void completed(V result, A attachment);
	void failed(Throwable exc, A attachment); 
}
