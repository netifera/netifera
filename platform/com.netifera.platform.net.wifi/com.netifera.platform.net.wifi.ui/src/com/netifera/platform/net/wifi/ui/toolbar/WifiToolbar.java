package com.netifera.platform.net.wifi.ui.toolbar;

import java.util.Collection;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.wifi.pcap.IWirelessCaptureInterface;
import com.netifera.platform.net.wifi.sniffing.IWifiSniffingDaemon;
import com.netifera.platform.net.wifi.ui.Activator;



public class WifiToolbar {
	
	private final RGB WARNING_COLOR = new RGB(0xF5, 0xA9, 0xA9);

	private final StartWifiSnifferAction startAction;
	private final IContributionItem startActionItem;
	private final StopWifiSnifferAction stopAction;
	private final ConfigureWifiSnifferAction configureAction;
	private final OpenCaptureFileAction captureAction;
	
	private final IContributionItem stopActionItem;
	private final ActionContributionItem configureActionItem;
	private final IContributionItem captureActionItem;
	private final IToolBarManager toolbarManager;
	
	public WifiToolbar(IToolBarManager manager) {
		toolbarManager = manager;
		startAction = new StartWifiSnifferAction(this);
		startActionItem = new ActionContributionItem(startAction);
		stopAction = new StopWifiSnifferAction(this);
		stopActionItem = new ActionContributionItem(stopAction);
		configureAction = new ConfigureWifiSnifferAction(this);
		configureActionItem = new ActionContributionItem(configureAction);
		captureAction = new OpenCaptureFileAction(this);
		captureActionItem = new ActionContributionItem(captureAction);
		
		manager.add(stopActionItem);
		manager.add(startActionItem);
		manager.add(configureActionItem);
		manager.add(captureActionItem);
		
		
	}
	
	public void dispose() {
		
	}
	public IToolBarManager getToolBar() {
		return toolbarManager;
	}
	
	public Point getConfigDialogLocation() {
		 Widget widget = configureActionItem.getWidget();
		 if(!(widget instanceof ToolItem))
			 return null;
		 ToolItem item = (ToolItem) widget;
			 
		 int x = item.getBounds().x;
		 int y = item.getBounds().y + item.getBounds().height;
		 Point p = item.getDisplay().map(item.getParent(), null, x, y);
		 return p;
		 
	}
	public void setFailed(final String message) {
		
		IContributionItem snifferLabelItem = new ControlContribution("sniffer_text") {
			@Override
			protected Control createControl(Composite parent) {
				final CLabel label = new CLabel(parent, SWT.CENTER);
				label.setBackground(new Color(parent.getDisplay(), WARNING_COLOR));
				label.setText("   " + message + "   ");
				label.pack(true);
				return label;
			}
			
		};
		
		toolbarManager.insertAfter(OpenCaptureFileAction.ID, snifferLabelItem);
		toolbarManager.update(true);		
	}
	
	public void asynchSetState() {
		if(PlatformUI.getWorkbench().getDisplay().isDisposed())
			return;
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setState();	
			}
		});
	}
	
	public void setState() {
		removeLabelIfExists();
		enableAll();
		
		final IProbe probe = Activator.getDefault().getCurrentProbe();
		if(probe == null) {
			failAll("No probe found!");
			return;
		}
		
		if(!probe.isConnected()) {
			failAll("Probe for this space is currently disconnected");
			return;
		}
		
		final IWifiSniffingDaemon wifiDaemon = Activator.getDefault().getWifiDaemon();
		if(wifiDaemon == null) {
			failAll("No Wireless Service found.");
			return;
		}
		
		final Collection<IWirelessCaptureInterface> wifiInterfaces = wifiDaemon.getWirelessInterfaces();
		if(wifiInterfaces == null) {
			failAll("No Wireless service found on remote probe");
			return;
		}
		
		if(wifiInterfaces.isEmpty()) {
			failLive("No 'monitor-mode' capable wireless devices found");
			if(wifiDaemon.isRunning()) {
				wifiDaemon.stop();
			}
			return;
		}
		
		if(!hasEnabledInterface(wifiInterfaces)) {
			failLive("None of the interfaces are available");
			if(wifiDaemon.isRunning())
				wifiDaemon.stop();
			return;
		}
		
	
	
		if(wifiDaemon.isRunning()) {
			disableConfigAndCapture();
			startAction.setEnabled(false);
		}  else {
			stopAction.setEnabled(false);
		}
			
	}
	private void removeLabelIfExists() {
		final IContributionItem item = toolbarManager.remove("sniffer_text");
		if(item != null) {
			item.dispose();
			toolbarManager.update(true);
		}
	}
	private void failAll(String message) {
		setFailed(message);
		disableAll();
	}
	
	private void failLive(String message) {
		setFailed(message);
		startAction.setEnabled(false);
		stopAction.setEnabled(false);
	}
	
	private boolean hasEnabledInterface(Collection<IWirelessCaptureInterface> interfaces) {
		for(ICaptureInterface iface : interfaces) {
			if(iface.captureAvailable())
				return true;
		}
		return false;
	}
	public void disableConfigAndCapture() {
		captureAction.setEnabled(false);
		configureAction.setEnabled(false);
	}
	
	public void enableConfigAndCapture() {
		captureAction.setEnabled(true);
		configureAction.setEnabled(true);
	}
	
	public void disableAll() {
		stopAction.setEnabled(false);
		startAction.setEnabled(false);
		configureAction.setEnabled(false);
		captureAction.setEnabled(false);
	}
	
	private void enableAll() {
		stopAction.setEnabled(true);
		startAction.setEnabled(true);
		configureAction.setEnabled(true);
		captureAction.setEnabled(true);
	}

}
