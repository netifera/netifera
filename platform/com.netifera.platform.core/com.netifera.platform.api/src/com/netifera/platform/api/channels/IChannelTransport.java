package com.netifera.platform.api.channels;

import java.io.InputStream;
import java.io.OutputStream;

public interface IChannelTransport {
	boolean isConnected();
	
	InputStream getInputStream();
	
	OutputStream getOutputStream();

}
