package com.netifera.platform.net.sockets;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Channel;


public interface AsynchronousChannel extends Channel, Closeable {
	void close() throws IOException;
}
