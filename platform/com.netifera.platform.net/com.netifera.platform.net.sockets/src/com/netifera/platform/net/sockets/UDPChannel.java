package com.netifera.platform.net.sockets;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.netifera.platform.net.sockets.internal.Activator;
import com.netifera.platform.net.sockets.internal.SocketEngineService;
import com.netifera.platform.util.addresses.inet.InternetAddress;
import com.netifera.platform.util.locators.UDPSocketLocator;

public class UDPChannel extends AsynchronousSocketChannel {

	public static UDPChannel open() throws IOException {
		return Activator.getInstance().getSocketEngine().openUDP();
	}

	public UDPChannel(SocketEngineService engine, DatagramChannel channel) {
		this.engine = engine;
		this.channel = channel;
	}

	@Override
	public DatagramChannel getWrappedChannel() {
		return (DatagramChannel)channel;
	}

	private DatagramSocket getSocket() {
		return getWrappedChannel().socket();
	}
	
	public void bind(UDPSocketLocator local) throws IOException {
		((DatagramChannel)channel).socket().bind(new InetSocketAddress(local.getAddress().toInetAddress(), local.getPort()));
	}
	
	public void connect(UDPSocketLocator remote) throws IOException {
		InetSocketAddress sockaddr = new InetSocketAddress(remote.getAddress().toInetAddress(), remote.getPort());
		((DatagramChannel)channel).connect(sockaddr);
		
		// workarounds for gnu classpath
		DatagramSocket socket = getSocket();
		if (!socket.isBound()) {
			socket.bind(null);
		}
		if (!socket.isConnected()) {
			socket.connect(sockaddr);
		}
	}

	public UDPSocketLocator getRemoteAddress() {
		try {
			if (getSocket().isClosed() || !getSocket().isConnected()) {
				return null;
			}
			InetSocketAddress address = (InetSocketAddress)getSocket().getRemoteSocketAddress();
			if (address == null) {
				return null;
			}
			return new UDPSocketLocator(InternetAddress.fromInetAddress(address.getAddress()), address.getPort());
		} catch (Exception e) { // FIXME or document
			// if an exception occurs trying to get the remote address, the socket was not connected
			return null;
		}
	}
	
	public UDPSocketLocator getLocalAddress() {
		try {
			if (getSocket().isClosed() || !getSocket().isBound()) {
				return null;
			}
			InetSocketAddress address = (InetSocketAddress)getSocket().getLocalSocketAddress();
			if (address == null) {
				return null;
			}
			return new UDPSocketLocator(InternetAddress.fromInetAddress(address.getAddress()), address.getPort());
		} catch (Exception e) { // FIXME or document
			// if an exception occurs trying to get the local address, the socket was not bound
			return null;
		}
	}

	public Future<UDPSocketLocator> receive(ByteBuffer dst) {
		return receive(dst, 30, TimeUnit.SECONDS, null, null);
	}

	public <A> Future<UDPSocketLocator> receive(ByteBuffer dst,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<UDPSocketLocator, ? super A> handler) {
		return engine.asynchronousReceive(this, dst, timeout, unit, attachment, handler);
	}

	public Future<Integer> send(ByteBuffer src, UDPSocketLocator target) {
		return send(src, target, 30, TimeUnit.SECONDS, null, null);
	}

	public <A> Future<Integer> send(ByteBuffer src, UDPSocketLocator target,
			long timeout, TimeUnit unit,
			A attachment, CompletionHandler<Integer, ? super A> handler) {
		return engine.asynchronousSend(this, src, target, timeout, unit, attachment, handler);
	}
	
	@Override
	public String toString() {
		UDPSocketLocator locator;
		StringBuilder sb = new StringBuilder(64);
		sb.append("udp/[");
		if (getSocket().isClosed()) {
			sb.append("closed");
		} else {
			locator = getLocalAddress();
			if (locator == null) {
				sb.append("not bound");
			} else {
				sb.append(locator.getAddress().toString());
				sb.append(':');
				sb.append(locator.getPort());
			}
			sb.append(" <> ");
			locator = getRemoteAddress();
			if (locator == null) {
				sb.append("not connected");
			} else {
				sb.append(locator.getAddress().toString());
				sb.append(':');
				sb.append(locator.getPort());
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
