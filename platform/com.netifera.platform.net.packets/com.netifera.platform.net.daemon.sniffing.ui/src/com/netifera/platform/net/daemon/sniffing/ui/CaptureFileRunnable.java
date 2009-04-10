package com.netifera.platform.net.daemon.sniffing.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;

public class CaptureFileRunnable implements IRunnableWithProgress {

	private final ICaptureFileInterface captureInterface;
	private final ISniffingDaemon sniffingDaemon;
	private final SniffingActionManager actionManager;
	private final ISpace space;
	private final Object lock = new Object();
	private final ILogger logger;
	private IProgressMonitor progressMonitor;
	private int lastPercent = 0;
	
	private boolean done;
	private boolean cancelled;
	
	CaptureFileRunnable(ICaptureFileInterface iface, ISniffingDaemon daemon,
			SniffingActionManager manager, ISpace space) {
		captureInterface = iface;
		sniffingDaemon = daemon;
		this.actionManager = manager;
		this.space = space;
		this.logger = Activator.getDefault().getLogManager().getLogger("Capture File");
	}
	
	public void run(IProgressMonitor monitor) throws 
			InterruptedException {
		this.progressMonitor = monitor;
		progressMonitor.beginTask("", 100);
		actionManager.disableAll();
		done = false;
		// XXX
		sniffingDaemon.runCaptureFile(space.getId(), captureInterface, new ICaptureFileProgress() {

			public void error(String message, Throwable e) {
				handleError(message);				
			}

			public void finished() {
				handleFinished();				
			}

			public boolean updateProgress(int percent, int count) {
				return handleUpdateProgress(percent, count);				
			}
			
		});
		
		synchronized(lock) {
			while(!done && !cancelled) {
				lock.wait();
			}
		}
		if(cancelled) {
			throw new InterruptedException();
		}

	}
	
	private void handleError(String message) {
		progressMonitor.done();
		synchronized(lock) {
			done = true;
			lock.notifyAll();
		}
		
		actionManager.asynchSetState();
		logger.error("Error parsing capture file : " + message);
		
	}
	
	private void handleFinished() {
		progressMonitor.done();
		synchronized(lock) {
			done = true;
			lock.notifyAll();
		}
		actionManager.asynchSetState();
	}
	
	private boolean handleUpdateProgress(int percent, int count) {
		if(PlatformUI.getWorkbench().getDisplay().isDisposed() || progressMonitor.isCanceled()) {
			synchronized (lock) {
				cancelled = true;
				lock.notifyAll();
				return true;
			}
		}
		if(percent > lastPercent) {
			progressMonitor.worked(percent - lastPercent);
			lastPercent = percent;
		}
		progressMonitor.setTaskName("Parsed " + count + " packets (" + percent + "% )");
		if(progressMonitor.isCanceled()) {
			progressMonitor.done();
			synchronized(lock) {
				done = true;
				lock.notifyAll();
			}
			actionManager.setState();
			return false;
		} else {
			return true;
		}
		
	}

}
