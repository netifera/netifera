package com.netifera.platform.net.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.netifera.platform.net.sockets.internal.Activator;
import com.netifera.platform.net.sockets.internal.SocketEngineService;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.TCPSocketLocator;

public class TCPChannel extends AsynchronousSocketChannel {

	public static TCPChannel open() throws IOException {
		return Activator.getInstance().getSocketEngine().openTCP();
	}
	
	public TCPChannel(SocketEngineService engine, SocketChannel channel) {
		this.engine = engine;
		this.channel = channel;
	}

	@Override
	public SocketChannel getWrappedChannel() {
		return (SocketChannel)channel;
	}
	
	private Socket getSocket() {
		return getWrappedChannel().socket();
	}
	
	public void bind(TCPSocketLocator local) throws IOException {
		getSocket().bind(new InetSocketAddress(local.getAddress().toInetAddress(), local.getPort()));
	}

	public <A> Future<Void> connect(TCPSocketLocator remote,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<Void, ? super A> handler) throws IOException, InterruptedException {
		return engine.asynchronousConnect(this, remote, timeout, unit, attachment, handler);
	}

	public TCPSocketLocator getRemoteAddress() {
		InetSocketAddress address = (InetSocketAddress)getSocket().getRemoteSocketAddress();
		if (address == null) {
			return null;
		}
		return new TCPSocketLocator(InternetAddress.fromInetAddress(address.getAddress()), address.getPort());
	}
	
	public TCPSocketLocator getLocalAddress() {
		if (!getSocket().isBound()) {
			return null;
		}
		InetSocketAddress address = (InetSocketAddress)getSocket().getLocalSocketAddress();
		if (address == null) {
			return null;
		}
		return new TCPSocketLocator(InternetAddress.fromInetAddress(address.getAddress()), address.getPort());
	}

	private static String getAddrPort(TCPSocketLocator locator) {
		return locator.getAddress().toString() + ':' + locator.getPort();
	}
	
	@Override
	public String toString() {
		if (getSocket().isClosed()) {
			return "tcp/[closed]";
		}
		if (!getSocket().isConnected()) {
			return "tcp/[not connected]";
		}
		return "tcp/[" + getAddrPort(getLocalAddress()) + " <> " + getAddrPort(getRemoteAddress()) + ']';
	}
}
