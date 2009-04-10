package com.netifera.platform.net.internal.daemon.sniffing;

import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.net.internal.daemon.probe.CaptureFileProgress;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;

public class CaptureFileRunnable implements Runnable {
	private final static long SEND_INTERVAL = 20;
	private final IMessenger messenger;
	private final ICaptureFileInterface captureFileInterface;
	private final ICaptureFileProgress progress;
	private final SniffingDaemon daemon;
	private final long spaceId;
	public CaptureFileRunnable(IMessenger messenger, ICaptureFileInterface iface,
				SniffingDaemon daemon, long spaceId) {
		this.messenger = messenger;
		this.progress = createProgress();
		this.captureFileInterface = iface;
		this.daemon = daemon;
		this.spaceId = spaceId;
	}

	public ICaptureFileProgress getProgress() {
		return progress;
	}
	private ICaptureFileProgress createProgress() {
		return new ICaptureFileProgress() {
			private long messageCounter = 0;
			public void error(String message, Throwable e) {
				sendMessage(CaptureFileProgress.createError(message));
				// XXX
			}

			public void finished() {
				sendMessage(CaptureFileProgress.createFinished());				
			}

			public boolean updateProgress(int percent, int count) {
				messageCounter++;
				if(messageCounter % SEND_INTERVAL == 0) {
					sendMessage(CaptureFileProgress.createUpdate(percent, count));
				}
				return true;
			}
			
			private void sendMessage(CaptureFileProgress progressMessage) {
				try {
					messenger.emitMessage(progressMessage);
				} catch (MessengerException e) {
					Thread.currentThread().interrupt();
				}
			}
			
		};
	}
	public void run() {
		daemon.runCaptureFile(spaceId, captureFileInterface, progress);
	}
}
