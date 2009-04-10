package com.netifera.platform.net.tools.auth;

import java.io.IOException;
import java.util.Iterator;

import com.netifera.platform.api.iterables.IndexedIterable;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tools.ITool;
import com.netifera.platform.api.tools.IToolContext;
import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.internal.tools.auth.Activator;
import com.netifera.platform.net.services.auth.AuthenticationListener;
import com.netifera.platform.net.services.auth.CredentialsVerifier;
import com.netifera.platform.net.services.credentials.Credential;

public abstract class AuthenticationBruteforcer implements ITool, AuthenticationListener {
	private IndexedIterable<Credential> credentials;
	private Iterator<Credential> credentialsIterator;
	private CredentialsVerifier verifier;

	protected IToolContext context;
	protected long realm;

	protected abstract CredentialsVerifier createCredentialsVerifier();
	public abstract IndexedIterable<Credential> defaultCredentials();
//	public abstract boolean isAuthenticableWith(Credential credential);

	public void toolRun(IToolContext context) throws ToolException {
		this.context = context;

		// XXX hardcode local probe as realm
		IProbe probe = Activator.getInstance().getProbeManager().getLocalProbe();
		realm = probe.getEntity().getId();

		setupToolOptions();
		context.setTotalWork(credentials.itemCount());

		context.info("Trying "+credentials.itemCount()+" credentials...");
		
		try {
			verifier = createCredentialsVerifier();
			verifier.tryCredentials(credentialsIterator, this);
		} catch (IOException e) {
			context.exception("I/O Error", e);
		} catch (InterruptedException e) {
			context.warning("Interrupted");
			Thread.currentThread().interrupt();
		} finally {
//			verifier.close();
			context.done();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void setupToolOptions() {
		credentials = (IndexedIterable<Credential>) context.getConfiguration().get("credentials");
		if (credentials == null)
			credentials = defaultCredentials();
		credentialsIterator = credentials.iterator();
	}

	public void authenticationError(Credential credential, Throwable e) {
		String msg = e.getLocalizedMessage();
		if (msg == null) msg = e.getMessage();
		if (msg == null) msg = e.toString();
		context.debug("Retrying '"+credential+"' after error: "+msg);
		verifier.retryCredential(credential);
	}

	public void authenticationFailed(Credential credential) {
		context.debug("Invalid credential: "+credential);
		context.worked(1);
	}

	public void authenticationSucceeded(Credential credential) {
		context.info("Found valid credential: "+credential);
		context.worked(1);
	}
}
