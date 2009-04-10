package com.netifera.platform.channel.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.netifera.platform.api.channels.IChannelTransport;

public class TCPSocketTransport implements IChannelTransport {
	
 
	/* simple factory to avoid throwing exception out of public constructor */
	public static IChannelTransport create(Socket socket) throws IOException {
		return new TCPSocketTransport(socket);
	}

	private final Socket socket;
	private final InputStream inputStream;
	private final OutputStream outputStream;
                                                                                                                                                                                                            
    /**                                                                                                                                                                                                     
     * Construct a new <code>TCPSocketChannel</code>.                                                                                                                                                       
     * @param socket <code>Socket</code> this channel refers to.                                                                                                                                            
     */                                                                                                                                                                                                     
    private TCPSocketTransport(Socket socket) throws IOException {
    	this.socket = socket;
    	verifySocketValidity();
    	inputStream = socket.getInputStream();
    	outputStream = socket.getOutputStream();    	                                                                                                                                                                                
    }

    private void verifySocketValidity() {
      	if(!socket.isConnected()) {
    		throw new IllegalArgumentException("Cannot create socket channel transport from disconnected socket");
    	}
    	if(socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
    		throw new IllegalArgumentException("Cannot create socket channel transport from closed socket");
    	}
    	
    }
    
    public boolean isConnected() {
    	return socket.isConnected();
    }
    
	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	};                                                                                                                                                                                                      
                                                                                           

}
