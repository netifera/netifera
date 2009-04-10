package com.netifera.platform.net.tools.portscanning;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.netifera.platform.api.tools.ToolException;
import com.netifera.platform.net.internal.tools.portscanning.Activator;
import com.netifera.platform.net.sockets.CompletionHandler;
import com.netifera.platform.net.sockets.UDPChannel;
import com.netifera.platform.tools.RequiredOptionMissingException;
import com.netifera.platform.util.PortSet;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.UDPSocketLocator;


public class UDPScanner extends AbstractPortscanner {
	Integer timeout;
	Integer delay;
	UDPChannel channel;

	@Override
	protected void setupToolOptions() throws ToolException {
		context.setTitle("UDP scan");
		super.setupToolOptions();
		timeout = (Integer) context.getConfiguration().get("timeout");
		if (timeout == null)
			throw new RequiredOptionMissingException("timeout");
		delay = (Integer) context.getConfiguration().get("delay");
		if (delay == null)
			throw new RequiredOptionMissingException("delay");
	}
	
	@Override
	protected void scannerRun() throws ToolException {
		context.setTitle("UDP scan "+targetNetwork);
		context.setTotalWork(targetNetwork.itemCount()*targetPorts.itemCount()+1); //+1 in order to account for waiting responses after sending all requests
		try {
			channel = Activator.getInstance().getSocketEngine().openUDP();
//			channel.setReuseAddress(true);
//			channel.bind(new UDPSocketLocator(IPv4Address.any, 53));
			
			readResponses();
			
			scanAllAddresses(channel);
			
			context.setStatus("Waiting responses for "+timeout+" seconds...");
			Thread.sleep(timeout*1000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			context.warning("Interrupted");
			return;
		} catch (IOException e) {
			context.exception("I/O Error", e);
		} finally {
			if (channel != null)
				try {
					channel.close();
				} catch (IOException e) {
					context.exception("I/O Error", e);
				}
		}
	}

	private void scanAllAddresses(UDPChannel channel) throws InterruptedException {
		ByteBuffer writeBuffer = ByteBuffer.allocate(4096);
		for (Integer port: targetPorts) {
			byte[] trigger = Activator.getInstance().getServerDetector().getTrigger("udp",port);
//			context.debug("Trigger for port "+port+": "+trigger);
			writeBuffer.clear();
			writeBuffer.put(trigger);
			writeBuffer.flip();
			context.setStatus("Scanning "+targetNetwork+":"+port+"/udp");
			for (InternetAddress address: targetNetwork) {
				if (Thread.currentThread().isInterrupted())
					throw new InterruptedException();
				try {
					writeBuffer.rewind();
					channel.send(writeBuffer, new UDPSocketLocator(address, port)).get();
					Thread.sleep(delay);
				} catch (ExecutionException e) {
					if (e.getCause().getClass() == SocketException.class) {
						context.warning(e.getCause().getMessage() + " for " +
								address + ":" + port + "/udp");
					} else {
						context.exception("Exception", e);
					}
				} finally {
					context.worked(1);
				}
			}
		}
	}
	
	private void readResponses() {
		final ByteBuffer responseBuffer = ByteBuffer.allocate(4096);
		channel.receive(responseBuffer, 1, TimeUnit.SECONDS, null, new CompletionHandler<UDPSocketLocator,Void>() {
			public void cancelled(Void attachment) {
				context.debug("Receive operation cancelled");
			}
			public void completed(UDPSocketLocator peer, Void attachment) {
				context.debug("Received response from "+peer);
				
				PortSet ports = new PortSet();
				ports.addPort(peer.getPort());
				Activator.getInstance().getNetworkEntityFactory().addOpenUDPPorts(realm, context.getSpaceId(), peer.getAddress(), ports);
				
				responseBuffer.flip();
				
				byte[] trigger = Activator.getInstance().getServerDetector().getTrigger("udp",peer.getPort());
				Map<String,String> serviceInfo = Activator.getInstance().getServerDetector().detect("udp",peer.getPort(), ByteBuffer.wrap(trigger), responseBuffer);
				if (serviceInfo != null) {
					Activator.getInstance().getNetworkEntityFactory().createService(realm, context.getSpaceId(), peer, serviceInfo.get("serviceType"), serviceInfo);
					context.info(serviceInfo.get("serviceType")+" @ "+peer);
				} else {
					context.warning("Unknown service @ " + peer);
				}

				responseBuffer.clear();
				channel.receive(responseBuffer, 1, TimeUnit.SECONDS, attachment, this);
			}
			public void failed(Throwable e, Void attachment) {
				if (e instanceof SocketTimeoutException) {
					channel.receive(responseBuffer, 1, TimeUnit.SECONDS, attachment, this);
				} else {
					context.exception("Exception",e);
				}
			}
		});
	}
}
