package com.netifera.platform.net.sockets.internal;

import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.net.sockets.CompletionHandler;

public class SelectionFuture<V,A> extends FutureTask<V> {

	final CompletionHandler<V,? super A> handler;
	final A attachment;
	final long deadline;
	private final ILogger logger;

	public SelectionFuture(CompletionHandler<V,? super A> handler, A attachment, long deadline, ILogger logger, Callable<V> callable) {
		super(callable);
		this.deadline = deadline;
		this.handler = handler;
		this.attachment = attachment;
		this.logger = logger;
	}

	@Override
	protected void done() {
		if (handler == null) return;

		try {
			if (this.isCancelled())
				handler.cancelled(attachment);
			else {
				final V value;
				try {
					value = get();
				} catch (InterruptedException e) {
					handler.failed(e, attachment);
					Thread.interrupted();
					return;
				} catch (ExecutionException e) {
					handler.failed(e.getCause(), attachment);
					return;
				}
				handler.completed(value, attachment);
			}
		} catch (Throwable e) {
			//NOTE if we dont do this, it will eat the exceptions
			logger.error("Unhandled exception in SelectionFuture", e);
			// FIXME re-throw e?
		}
	}
	
	public long getDeadline() {
		return deadline;
	}
	
	public boolean testTimeOut(long now) {
		if (deadline > now) return false;
		setException(new SocketTimeoutException());
		return true;
	}
}
