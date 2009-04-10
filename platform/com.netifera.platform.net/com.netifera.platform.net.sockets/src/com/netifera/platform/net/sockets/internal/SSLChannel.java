package com.netifera.platform.net.sockets.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import com.netifera.platform.net.sockets.AsynchronousChannel;
import com.netifera.platform.net.sockets.TCPChannel;

/**
 * Upgrade a ByteChannel for SSL.
 * 
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v1.0.0 - First public release.</li>
 * </ul>
 * 
 * <p>
 * This source code is given to the Public Domain. Do what you want with it.
 * This software comes with no guarantees or warranties. Please visit <a
 * href="http://perso.wanadoo.fr/reuse/sslbytechannel/">http://perso.wanadoo.fr/reuse/sslbytechannel/</a>
 * periodically to check for updates or to contribute improvements.
 * </p>
 * 
 * @author David Crosson
 * @author david.crosson@wanadoo.fr
 * @version 1.0.0
 */
public class SSLChannel implements ByteChannel, AsynchronousChannel {
	private TCPChannel wrappedChannel;
	private boolean closed = false;
	private SSLEngine engine;

	private final ByteBuffer inAppData;
	private final ByteBuffer outAppData;

	private final ByteBuffer inNetData;
	private final ByteBuffer outNetData;

	/**
	 * Creates a new instance of SSLChannel
	 * 
	 * @param wrappedChannel
	 *            The byte channel on which this ssl channel is built. This
	 *            channel contains encrypted data.
	 * @param engine
	 *            A SSLEngine instance that will remember SSL current context.
	 *            Warning, such an instance CAN NOT be shared between multiple
	 *            SSLChannel.
	 */
	public SSLChannel(TCPChannel wrappedChannel, SSLEngine engine) {
		this.wrappedChannel = wrappedChannel;
		this.engine = engine;

		SSLSession session = engine.getSession();
		inAppData = ByteBuffer.allocate(session.getApplicationBufferSize());
		outAppData = ByteBuffer.allocate(session.getApplicationBufferSize());

		inNetData = ByteBuffer.allocate(session.getPacketBufferSize());
		outNetData = ByteBuffer.allocate(session.getPacketBufferSize());
	}

	/**
	 * Ends SSL operation and close the wrapped byte channel
	 * 
	 * @throws java.io.IOException
	 *             May be raised by close operation on wrapped byte channel
	 */
	public void close() throws java.io.IOException {
		if (!closed) {
			try {
				engine.closeOutbound();
				sslLoop(wrap());
				wrappedChannel.close();
			} finally {
				closed = true;
			}
		}
	}

	/**
	 * Is the channel open ?
	 * 
	 * @return true if the channel is still open
	 */
	public boolean isOpen() {
		return !closed;
	}

	/**
	 * Fill the given buffer with some bytes and return the number of bytes
	 * added in the buffer.<br>
	 * This method may return immediately with nothing added in the buffer. This
	 * method must be use exactly in the same way of ByteChannel read operation,
	 * so be careful with buffer position, limit, ... Check corresponding
	 * javadoc.
	 * 
	 * @param byteBuffer
	 *            The buffer that will received read bytes
	 * @throws java.io.IOException
	 *             May be raised by ByteChannel read operation
	 * @return The number of bytes read
	 */
	public int read(java.nio.ByteBuffer byteBuffer) throws java.io.IOException {
		if (isOpen()) {
			try {
				@SuppressWarnings("unused")
				SSLEngineResult r = sslLoop(unwrap());
			} catch (SSLException e) {
				System.err.println("SSLException while reading " + e);// TODO
																		// :
																		// Better
																		// SSL
																		// Exception
																		// management
																		// must
																		// be
																		// done
			} catch (ClosedChannelException e) {
				close();
			}
		}

		inAppData.flip();
		int posBefore = inAppData.position();
		byteBuffer.put(inAppData);
		int posAfter = inAppData.position();
		inAppData.compact();

		if (posAfter - posBefore > 0)
			return posAfter - posBefore;
		if (isOpen())
			return 0;
		else
			return -1;
	}

	/**
	 * Write remaining bytes of the given byte buffer. This method may return
	 * immediately with nothing written. This method must be use exactly in the
	 * same way of ByteChannel write operation, so be careful with buffer
	 * position, limit, ... Check corresponding javadoc.
	 * 
	 * @param byteBuffer
	 *            buffer with remaining bytes to write
	 * @throws java.io.IOException
	 *             May be raised by ByteChannel write operation
	 * @return The number of bytes written
	 */
	public int write(java.nio.ByteBuffer byteBuffer) throws java.io.IOException {
		if (!isOpen())
			return 0;
		int posBefore, posAfter;
		posBefore = byteBuffer.position();
		if (byteBuffer.remaining() < outAppData.remaining()) {
			outAppData.put(byteBuffer); // throw a BufferOverflowException if
										// byteBuffer.remaining() >
										// outAppData.remaining()
		} else {
			while (byteBuffer.hasRemaining() && outAppData.hasRemaining()) {
				outAppData.put(byteBuffer.get());
			}
		}
		posAfter = byteBuffer.position();

		if (isOpen()) {
			try {
				while (true) {
					SSLEngineResult r = sslLoop(wrap());
					// System.out.println(r);
					if (r.bytesConsumed() == 0 && r.bytesProduced() == 0)
						break;
				}
				;
			} catch (SSLException e) {
				e.printStackTrace();
				System.err.println("SSLException while reading " + e); // TODO
																		// :
																		// Better
																		// SSL
																		// Exception
																		// management
																		// must
																		// be
																		// done
			} catch (ClosedChannelException e) {
				e.printStackTrace();
				close();
			}
		}

		return posAfter - posBefore;
	}

	private SSLEngineResult unwrap() throws IOException, SSLException {
		try {
			while (wrappedChannel.read(inNetData).get() > 0)
				;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		inNetData.flip();
		SSLEngineResult ser = engine.unwrap(inNetData, inAppData);
		inNetData.compact();

		// System.err.println(ser);
		return ser;
	}

	private SSLEngineResult wrap() throws IOException, SSLException {
		SSLEngineResult ser = null;

		outAppData.flip();
		ser = engine.wrap(outAppData, outNetData);
		outAppData.compact();

		outNetData.flip();
		while (outNetData.hasRemaining())
			try {
				wrappedChannel.write(outNetData).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		outNetData.compact();

		// System.err.println(ser);
		return ser;
	}

	@SuppressWarnings({ "incomplete-switch", "fallthrough" })
	private SSLEngineResult sslLoop(SSLEngineResult ser) throws SSLException,
			IOException {
		System.out.println("sslLoop " + ser);
		if (ser == null)
			return ser;
		System.err.println(String.format("%s - %s\n", ser.getStatus()
				.toString(), ser.getHandshakeStatus().toString()));
		while (ser.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED
				&& ser.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
			switch (ser.getHandshakeStatus()) {
			case NEED_TASK:
				// Executor exec = Executors.newSingleThreadExecutor();
				Runnable task;
				while ((task = engine.getDelegatedTask()) != null) {
					// exec.execute(task);
					task.run();
				}
				// Must continue with wrap as data must be sent
			case NEED_WRAP:
				ser = wrap();
				break;
			case NEED_UNWRAP:
				ser = unwrap();
				break;
			}
		}
		switch (ser.getStatus()) {
		case CLOSED:
			System.err
					.println("SSLEngine operations finishes, closing the socket");
			try {
				wrappedChannel.close();
			} finally {
				closed = true;
			}
			break;
		}
		return ser;
	}
}
