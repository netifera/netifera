package com.netifera.platform.host.internal.processes.ui;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.host.processes.IProcessManagerFactory;
import com.netifera.platform.ui.images.ImageCache;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.util.BalloonManager;

public class Activator implements BundleActivator {
	public final static String PLUGIN_ID = "com.netifera.platform.host.processes.ui";

	private static Activator instance;
	private ImageCache imageCache;
	private BalloonManager balloonManager;
	private ServiceTracker processManagerFactoryTracker;
	private ServiceTracker probeManagerTracker;

	public static Activator getInstance() {
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		instance = this;
		imageCache = new ImageCache(PLUGIN_ID);
		
		processManagerFactoryTracker = new ServiceTracker(context, IProcessManagerFactory.class.getName(), null);
		processManagerFactoryTracker.open();
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		imageCache.dispose();
		imageCache = null;
	}

	public ImageCache getImageCache() {
		return imageCache;
	}

	public synchronized BalloonManager getBalloonManager() {
		if (balloonManager == null)
			balloonManager = new BalloonManager();
		return balloonManager;
	}

	public IProcessManagerFactory getProcessManagerFactory() {
		return (IProcessManagerFactory) processManagerFactoryTracker.getService();
	}
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public ISpace getCurrentSpace() {
		IWorkbenchPage page = getWindow().getActivePage();
		if(page == null) {
			return null;
		}
		IEditorPart editor = page.getActiveEditor();
		if(editor == null || !(editor.getEditorInput() instanceof SpaceEditorInput)) {
			return null;
		}
		
		return ((SpaceEditorInput)editor.getEditorInput()).getSpace();
	}

	public IProbe getCurrentProbe() {
		final ISpace space = getCurrentSpace();
		if(space == null)
			return null;
		return getProbeManager().getProbeById(getCurrentSpace().getProbeId());
	}

	private IWorkbenchWindow getWindow() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		if(windows.length == 0) {
			return null;
		}
		return windows[0];
	}
}
