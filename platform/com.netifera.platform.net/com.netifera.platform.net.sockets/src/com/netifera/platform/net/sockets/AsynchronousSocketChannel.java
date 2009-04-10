package com.netifera.platform.net.sockets;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class AsynchronousSocketChannel extends AsynchronousSelectableChannel implements AsynchronousByteChannel {
	
	public Future<Integer> read(ByteBuffer dst) {
		return read(dst, 30, TimeUnit.SECONDS, null, null);
	}

	public <A> Future<Integer> read(ByteBuffer dst,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<Integer, ? super A> handler) {
		return engine.asynchronousRead(this, dst, timeout, unit, attachment, handler);
	}

	public Future<Integer> write(ByteBuffer src) {
		return write(src, 30, TimeUnit.SECONDS, null, null);
	}

	public <A> Future<Integer> write(ByteBuffer src,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<Integer, ? super A> handler) {
		return engine.asynchronousWrite(this, src, timeout, unit, attachment, handler);
	}
}
