package com.netifera.platform.internal.channel.tcplisten;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.netifera.platform.api.channels.IChannelConnectProgress;
import com.netifera.platform.api.dispatcher.IClientDispatcher;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.channel.socket.TCPSocketTransport;
import com.netifera.platform.channel.tcplisten.ITCPListenConnecter;

public class TCPListenConnecter implements ITCPListenConnecter {
                                                                                                                                                                                                       
	private InetSocketAddress remoteAddress;
    private final Socket socket;
    
    private IClientDispatcher dispatcher;
    private Thread connectThread;        
    private boolean connectInProgress;
                                                                                                                                                                                                   
    public TCPListenConnecter(IClientDispatcher dispatcher) {
    		this.dispatcher = dispatcher;
            this.socket = new Socket();
    }        
    

    public void setAddress(InetSocketAddress address) {
    	this.remoteAddress = address;
    }
    
	
	public void abortConnect() {
		if(connectThread != null && connectInProgress) {
			connectThread.interrupt();
		}
		
	}
	
	public void connect(final IChannelConnectProgress progress) {
		if(remoteAddress == null) {
			progress.connectFailed("Cannot connect before configuring channel address.  Call setAddress() first.", null);
			return;
		}
		
		connectThread = new Thread(new Runnable() {
			public void run() {
				runConnect(progress);
			}
		});
		connectThread.start();   	
	}
	
	
	private void runConnect(IChannelConnectProgress progress) {
		try {
			doConnect(progress);
		} catch(InterruptedException e) {
			progress.connectFailed("Connection Aborted", null);
		} catch(IOException e) {
			progress.connectFailed("Connection Failed", e);
		}
	}
	private void doConnect(IChannelConnectProgress progress) throws InterruptedException, IOException {
	    /*                                                                                                                                                                                      
         * This is the code we would use to have interruptible connects.                                                                                                                        
         * Unfortunately it does not seem to work and causes the object                                                                                                                         
         * streams to block                                                                                                                                                                     
         */                                                                                                                                                                                     
        
    	/*
        socketChannel = SocketChannel.open();                                                                                                                                                 
        socketChannel.connect(address);                                                                                                                                                       
        socket = socketChannel.socket();
        */                                                                                                                                                    
                 
		connectInProgress = true;
        socket.connect(remoteAddress);   
        connectInProgress = false;
        final IMessenger messenger = dispatcher.createMessenger(TCPSocketTransport.create(socket));
        progress.connectCompleted(messenger);
		
		
	}

}
