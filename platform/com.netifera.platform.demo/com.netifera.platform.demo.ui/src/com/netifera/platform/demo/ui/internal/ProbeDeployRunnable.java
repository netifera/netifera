package com.netifera.platform.demo.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.channels.IChannelConnectProgress;
import com.netifera.platform.api.channels.IChannelConnecter;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.IWorkspace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.net.model.INetworkEntityFactory;
import com.netifera.platform.net.model.InternetAddressEntity;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.util.addresses.inet.InternetAddress;

public class ProbeDeployRunnable implements IRunnableWithProgress, IChannelConnectProgress {

	private final static String DEMO_PROBE_CHANNEL_CONFIG = "tcplisten:0.0.0.0:31337";
	private final SocketChannel socket;
	private final File probeFile;
	private final IChannelConnecter connecter;
	private final long realmId;
	private final long spaceId;
	private final InetAddress probeAddress;
	private final UIProbeDeployer deployer;
	private volatile boolean connectFinished;
	private String probeName;
	private IWorkbenchPage page;
	
	ProbeDeployRunnable(SocketChannel socket, File probeFile, 
			IChannelConnecter connecter, UIProbeDeployer deployer, long realmId, long spaceId) {
		this.socket = socket;
		this.probeFile = probeFile;
		this.connecter = connecter;
		this.deployer = deployer;
		this.realmId = realmId;
		this.spaceId = spaceId;
	
		setActivePage();
		
		SocketAddress address = socket.socket().getRemoteSocketAddress();
		if(address instanceof InetSocketAddress) {
			InetSocketAddress socketAddress = (InetSocketAddress) address;
			probeAddress = socketAddress.getAddress();
			probeName = "Remote Probe [" + socketAddress.getAddress().toString() + "]";
		} else {
			throw new IllegalArgumentException("Unknown socket address type");
		}
	}
	
	private void setActivePage() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			}
		});
		
	}
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		connectFinished = false;
		connecter.connect(this);
		try {
			runTransfer(monitor);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
		
	}
	
	public void runTransfer(IProgressMonitor monitor) throws IOException {
		FileInputStream fis = new FileInputStream(probeFile);
		FileChannel fileChannel = fis.getChannel();
		
		final int probeSize = (int) probeFile.length();
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		buffer.putInt(probeSize);
		
		monitor.beginTask("Transfer Probe", probeSize);
		int bytesSent = 0;
		
		while(!monitor.isCanceled()) {
			if(fileChannel.read(buffer) == -1)
				break;
			buffer.flip();
			while(buffer.remaining() > 0) {
				int n = socket.write(buffer);
				bytesSent += n;
				monitor.worked(n);
				monitor.setTaskName("Transfer Probe (" + numberToString(bytesSent) + " of " + 
						numberToString(probeSize) + ")");
			}
			buffer.clear();
		}
		
		waitConnectBack(monitor);
		monitor.done();
		fileChannel.close();
		socket.close();

	}
	
	int percent(int total, int part) {
		return part * 100 / total;
	}
	
	String numberToString(int n) {
		if(n < 1024)
			return Integer.toString(n);
		else if(n < 1024 * 1024)
			return Integer.toString(n / 1024) + "KB";
		else
			return String.format("%.2fMB", n / (1024.0 * 1024.0));
		
	}
	
	private void waitConnectBack(IProgressMonitor monitor) {
		int i = 1;
		while(!connectFinished && !monitor.isCanceled()) {
			try {				
				monitor.setTaskName("Waiting for connect back (" + i + " seconds)");
				Thread.sleep(1000);
				i++;
			} catch (InterruptedException e) {
				return;
			}
		}
		if(monitor.isCanceled())
			return;
		monitor.setTaskName("Probe Deployed!");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		
	}
	public void connectCompleted(IMessenger channelMessenger) {
		connectFinished = true;
		final INetworkEntityFactory factory = deployer.getNetworkEntityFactory();
		InternetAddress address = InternetAddress.fromInetAddress(probeAddress);
		InternetAddressEntity addressEntity = factory.createAddress(realmId, spaceId, address);
		String config = "tcplisten:" + address.toStringLiteral() + ":31337";
		final IProbe probe = deployer.getProbeManager().createProbe(addressEntity.getHost(), probeName, config, spaceId);
		probe.setConnected(channelMessenger);
		connectFinished = true;
		
	}
	public void connectFailed(String reason, Throwable exception) {
		deployer.getLogger().warning("Connect failed : " + reason, exception);
		connectFinished = true;
	}
	public void connectUpdate(String update) {
		
	}

}
