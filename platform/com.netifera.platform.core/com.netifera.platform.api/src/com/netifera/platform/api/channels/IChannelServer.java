package com.netifera.platform.api.channels;

import java.io.IOException;

public interface IChannelServer {
	
	public void startListening() throws IOException;
	public void stopListening();

}
