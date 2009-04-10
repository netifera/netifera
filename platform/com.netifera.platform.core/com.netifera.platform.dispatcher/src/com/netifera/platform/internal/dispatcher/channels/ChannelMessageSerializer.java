package com.netifera.platform.internal.dispatcher.channels;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.net.SocketException;

import com.netifera.platform.api.channels.ChannelException;
import com.netifera.platform.api.channels.IChannelMessageSerializer;
import com.netifera.platform.api.channels.IChannelTransport;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.ProbeMessage;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.dispatcher.channels.ChannelEOFException;

public class ChannelMessageSerializer implements IChannelMessageSerializer {
	
	private boolean isClosed;
	
	public static ChannelMessageSerializer createServerSerializer(IChannelTransport transport, ILogger logger) throws IOException {
		logger.debug("Initializing new server object serializer");
		final ObjectInputStream in = new ObjectInputStream(transport.getInputStream());
		final ObjectOutputStream out = new ObjectOutputStream(transport.getOutputStream());
		return new ChannelMessageSerializer(in, out, logger);
	}
	
	
	public static ChannelMessageSerializer createClientSerializer(IChannelTransport transport, ILogger logger) throws IOException {
		logger.debug("Initializing new client object serializer");
		final ObjectOutputStream out = new ObjectOutputStream(transport.getOutputStream());
		final ObjectInputStream in = new ObjectInputStream(transport.getInputStream());
		return new ChannelMessageSerializer(in, out, logger);
	}
	
	
	private final ObjectOutputStream objectOutStream;
	private final ObjectInputStream objectInStream;
	private final ILogger logger;
	
	private ChannelMessageSerializer(ObjectInputStream in, ObjectOutputStream out, ILogger logger) {
		this.objectInStream = in;
		this.objectOutStream = out;
		this.logger = logger;
	}
	
	public ProbeMessage readMessage() throws ChannelException {
		try {
			return readObject();
		} catch(ObjectStreamException e) {
			logger.error("ObjectStreamException reading from input stream ", e);
			throw new ChannelException("Disconnecting on channel error");
		} catch(EOFException e) {
			throw new ChannelEOFException();
		} catch(SocketException e) {
			throw new ChannelEOFException();
		} catch(IOException e) {
			logger.error("IOException reading probe message from channel", e);
			throw new ChannelException("Error reading from channel", e);
		} catch(ClassNotFoundException e) {
			logger.error("Class not found exception reading probe message", e);
			throw new ChannelException("Error reading from channel", e);
		} catch (Exception e) {
			logger.error("Unexpected exception reading from channel", e);
			throw new ChannelException("Unexpected error reading from channel", e);
		}
	}
	
	private ProbeMessage readObject() throws Exception {
		Object o = objectInStream.readObject();
		if(o instanceof ProbeMessage) {
			return (ProbeMessage)o;
		}
		
		logger.error("Unexpected object type reading from channel: " + o.getClass().getName());
		throw new ChannelException("Wrong object type read from channel " + o.getClass().getName());
	}
	
	public void sendMessage(IProbeMessage message) throws ChannelException {
		try {
			objectOutStream.reset();
			objectOutStream.writeObject(message);
		} catch(ObjectStreamException e) {
			logger.error("ObjectStreamException writing to channel", e);
			throw new ChannelException("Disconnecting on channel error");
		} catch (IOException e) {
			logger.error("IOException on channel write");
			throw new ChannelException("Disconnecting on channel error");
		}
	}
	
	public synchronized void close() {
		if(isClosed) 
			return;
		isClosed = true;
		
		try {
			objectInStream.close();
		} catch (IOException e) {
			logger.warning("Closing channel input object stream failed", e);
		}
		
		try {
			objectOutStream.close();
		} catch (IOException e) {
			logger.warning("Closing channel output object stream failed", e);
		}
		
	}

}
