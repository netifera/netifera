package com.netifera.platform.net.wifi.ui;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemonFactory;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemon;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemonFactory;
import com.netifera.platform.net.wifi.ui.toolbar.WifiToolbar;
import com.netifera.platform.ui.application.ApplicationPlugin;
import com.netifera.platform.ui.spaces.ISpaceEditor;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.workbench.IWorkbenchChangeListener;
import com.netifera.platform.ui.workbench.WorkbenchChangeManager;


public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.netifera.platform.net.wifi.ui";

	private static Activator plugin;
	
	private ServiceTracker sniffingDaemonFactoryTracker;
	private ServiceTracker wifiDaemonFactoryTracker;
	private ServiceTracker probeManagerTracker;
	private ServiceTracker logManagerTracker;
	
	private WifiToolbar wifiToolbar;
	private ToolBarContributionItem toolbarItem;
	private ISpaceChangeListener spaceChangeListener;
	
	private WorkbenchChangeManager workbenchChangeManager;
	
	public Activator() {
	}

	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		sniffingDaemonFactoryTracker = new ServiceTracker(context, ISniffingDaemonFactory.class.getName(), null);
		sniffingDaemonFactoryTracker.open();
		
		wifiDaemonFactoryTracker = new ServiceTracker(context, IWifiSniffingDaemonFactory.class.getName(), null);
		wifiDaemonFactoryTracker.open();
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		logManagerTracker = new ServiceTracker(context, ILogManager.class.getName(), null);
		logManagerTracker.open();
	}

	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}


	public void registerSpaceChangeListener(ISpaceChangeListener listener) {
		this.spaceChangeListener = listener;
	}
	
	public static Activator getDefault() {
		return plugin;
	}

	
	public ISniffingDaemonFactory getSniffingDaemonFactory() {
		return (ISniffingDaemonFactory) sniffingDaemonFactoryTracker.getService();
	}
	
	public IWifiSniffingDaemonFactory getWifiDaemonFactory() {
		return (IWifiSniffingDaemonFactory) wifiDaemonFactoryTracker.getService();
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public ILogManager getLogManager() {
		return (ILogManager) logManagerTracker.getService();
	}
	
	public ISniffingDaemon getSniffingDaemon() {
		IProbe probe = getCurrentProbe();
		if(probe == null)
			return null;
		// XXX fix with change handler
		return getSniffingDaemonFactory().createForProbe(probe, null);
	}
	

	public IWifiSniffingDaemon getWifiDaemon() {
		final IProbe probe = getCurrentProbe();
		if(probe == null) 
			return null;
		// XXX fix with change handler
		return getWifiDaemonFactory().createForProbe(probe, null);
	}
	
	public void initialize() {
		workbenchChangeManager = new WorkbenchChangeManager(getWindow(), PerspectiveFactory.ID, createChangeListener());
		workbenchChangeManager.initialize();
	}
	
	private IWorkbenchChangeListener createChangeListener() {
		return new IWorkbenchChangeListener() {

			public void activePageOpened(IWorkbenchPage page) {	
			}

			public void partChange() {
				if(wifiToolbar != null) {
					wifiToolbar.setState();
				}	
				if(spaceChangeListener != null) {
					spaceChangeListener.spaceChanged(getCurrentSpace());
				}
			}

			public void perspectiveClosed() {
				if(toolbarItem != null) {
					ApplicationPlugin.getDefault().getCoolBar().remove(toolbarItem);
					toolbarItem.dispose();
					toolbarItem = null;
					wifiToolbar.dispose();
					wifiToolbar = null;
				}
				
			}

			public void perspectiveOpened() {
				displayToolbar();
			}
			
		};
	}

	private void displayToolbar() {
		if(toolbarItem != null)
			return;
		final ICoolBarManager coolbar = ApplicationPlugin.getDefault().getCoolBar();
		final IToolBarManager toolbar = new ToolBarManager(coolbar.getStyle());
		toolbarItem = new ToolBarContributionItem(toolbar);
		coolbar.add(toolbarItem);
		wifiToolbar = new WifiToolbar(toolbar);
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				wifiToolbar.setState();
				coolbar.update(true);
				
			}		
		});	
	}
	
	
	public ISpace getCurrentSpace() {
		final IEditorPart editor = getCurrentEditor();
		if(editor == null || !(editor.getEditorInput() instanceof SpaceEditorInput)) {
			return null;
		}
		
		return ((SpaceEditorInput)editor.getEditorInput()).getSpace();
	}
	
	public IEditorPart getCurrentEditor() {
		final IWorkbenchPage page = getWindow().getActivePage();
		if(page == null) {
			return null;
		}
		return page.getActiveEditor();
	}
	
	public ISpaceEditor getCurrentSpaceEditor() {
		final IEditorPart editor = getCurrentEditor();
		if(editor instanceof ISpaceEditor)
			return (ISpaceEditor) editor;
		else
			return null;
		
	}
	

	public IProbe getCurrentProbe() {
		final ISpace space = getCurrentSpace();
		if(space == null)
			return null;
		return getProbeManager().getProbeById(getCurrentSpace().getProbeId());
	}

	private IWorkbenchWindow getWindow() {
		IWorkbenchWindow[] windows = getWorkbench().getWorkbenchWindows();
		if(windows.length == 0) {
			return null;
		}
		return windows[0];
	}

}
