package com.netifera.platform.net.services.auth;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.netifera.platform.net.services.credentials.Credential;
import com.netifera.platform.net.sockets.CompletionHandler;
import com.netifera.platform.net.sockets.TCPChannel;
import com.netifera.platform.util.locators.TCPSocketLocator;

public abstract class TCPCredentialsVerifier extends CredentialsVerifier {
	final private TCPSocketLocator locator;
	final private AtomicInteger connectionsCount = new AtomicInteger(0);

	public TCPCredentialsVerifier(TCPSocketLocator locator) {
		this.locator = locator;
	}
	
	protected abstract void authenticate(TCPChannel channel, Credential credential, long timeout, TimeUnit unit, CompletionHandler<Boolean,Credential> handler);

	private void spawnConnection() throws IOException, InterruptedException {
		Credential credential = nextCredentialOrNull();
		if (credential == null) return;
		
		final TCPChannel channel = TCPChannel.open();
		connectionsCount.incrementAndGet();
		channel.connect(locator, 5, TimeUnit.SECONDS, credential, new CompletionHandler<Void,Credential>() {
			private void closeChannel() {
				connectionsCount.decrementAndGet();
				try {
					channel.close();
				} catch (IOException e) {
				}
			}
			
			public void cancelled(Credential attachment) {
				closeChannel();
			}

			public void completed(Void result, Credential attachment) {
				authenticate(channel, attachment, 8, TimeUnit.SECONDS, new CompletionHandler<Boolean,Credential>() {
					public void cancelled(Credential attachment) {
						closeChannel();
					}
					public void completed(Boolean result,
							Credential attachment) {
						if (result) {
							listener.authenticationSucceeded(attachment);
							
							// close because now we're logged in, cannot login again with other credential in this connection
							closeChannel();
						} else {
							listener.authenticationFailed(attachment);

							// HACK to deal with socket engine not notifying back when some sockets are closed
							// once this is fixed, we could reuse connections and try to authenticate again with the next credential
							if (true) {
								closeChannel();
								return;
							}

							// and try next credential, try to reuse the connection
							Credential credential = nextCredentialOrNull();
							if (credential == null) {
								closeChannel();
								return;
							}
							authenticate(channel, credential, 8, TimeUnit.SECONDS, this);
						}
					}
					public void failed(Throwable exc,
							Credential attachment) {
						closeChannel();
						listener.authenticationError(attachment, exc);
//						retryCredential(attachment);
					}
				});
			}

			public void failed(Throwable exc, Credential attachment) {
				closeChannel();
				listener.authenticationError(attachment, exc);
//				retryCredential(attachment);
			}
		});					
	}
	
	@Override
	public void tryCredentials(Iterator<Credential> credentials, AuthenticationListener listener) throws IOException, InterruptedException {
		this.credentials = credentials;
		this.listener = listener;
		
		while (hasNextCredential() || connectionsCount.get() > 0) {
			while (connectionsCount.get()>10)
				Thread.sleep(500);
			if (hasNextCredential() && !Thread.currentThread().isInterrupted()) spawnConnection();
		}
	}
}
