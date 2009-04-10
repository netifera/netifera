package com.netifera.platform.net.sockets;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class LineChannel extends AsynchronousSocketChannel {
	final private AsynchronousByteChannel channel;
	private String separator = "\r\n";
	final private Charset charset = Charset.forName("UTF-8");
	final private StringBuffer inputBuffer = new StringBuffer();
	
	public LineChannel(AsynchronousByteChannel channel) {
		this.channel = channel;
	}
	
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public <A> void writeLine(String line, final long timeout, final TimeUnit unit, A attachment, final CompletionHandler<Void,A> handler) {
		final ByteBuffer buffer = charset.encode(line+separator);
		channel.write(buffer, timeout, unit, attachment, new CompletionHandler<Integer,A>() {
			public void cancelled(A attachment) {
				handler.cancelled(attachment);
			}

			public void completed(Integer result, A attachment) {
				if (buffer.hasRemaining())
					channel.write(buffer, timeout, unit, attachment, this); //FIXME timeout should decrement, not always the same
				handler.completed(null, attachment);
			}

			public void failed(Throwable exc, A attachment) {
				exc.printStackTrace();
				handler.failed(exc, attachment);
			}
		});
	}

	private String inputBufferReadLine() {
		int indexCR = inputBuffer.indexOf("\r");
		int indexNL = inputBuffer.indexOf("\n");
		int maxIndex = indexCR > indexNL ? indexCR : indexNL;
		if (maxIndex == -1)
			return null;
		int minIndex = indexCR == -1 ? indexNL : (indexCR < indexNL ? indexCR : indexNL);
		String line = inputBuffer.substring(0, minIndex);
		inputBuffer.replace(0, maxIndex+1, "");
		return line;
	}
	
	public <A> void readLine(final long timeout, final TimeUnit unit, A attachment, final CompletionHandler<String,A> handler) {
		final ByteBuffer buffer = ByteBuffer.allocate(1024);

		String line = inputBufferReadLine();
		if (line != null) {
			handler.completed(line, attachment);
			return;
		}
		
		channel.read(buffer, timeout, unit, attachment, new CompletionHandler<Integer,A>() {

			public void cancelled(A attachment) {
				handler.cancelled(attachment);
			}

			public void completed(Integer result, A attachment) {
				buffer.flip();
				inputBuffer.append(charset.decode(buffer).toString());
				String line = inputBufferReadLine();
				if (line == null)
					channel.read(buffer, timeout, unit, attachment, this);
				else {
					handler.completed(line, attachment);
				}
			}

			public void failed(Throwable exc, A attachment) {
				handler.failed(exc, attachment);
			}
		});
	}
	
	public void close() throws IOException {
		channel.close();
	}
}
