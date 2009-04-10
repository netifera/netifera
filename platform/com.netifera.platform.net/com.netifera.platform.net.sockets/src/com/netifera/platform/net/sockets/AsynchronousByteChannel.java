package com.netifera.platform.net.sockets;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface AsynchronousByteChannel extends AsynchronousChannel {
	Future<Integer> read(ByteBuffer dst);
	
	<A> Future<Integer> read(ByteBuffer dst,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<Integer, ? super A> handler);
	
	Future<Integer> write(ByteBuffer src);
	
	<A> Future<Integer> write(ByteBuffer src,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<Integer, ? super A> handler);
}
