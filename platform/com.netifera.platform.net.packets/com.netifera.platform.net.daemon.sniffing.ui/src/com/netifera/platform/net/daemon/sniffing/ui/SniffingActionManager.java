package com.netifera.platform.net.daemon.sniffing.ui;

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

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.pcap.ICaptureInterface;

public class SniffingActionManager {
	private final RGB WARNING_COLOR = new RGB(0xF5, 0xA9, 0xA9);

	private final StartSnifferAction startSnifferAction;
	private final StopSniffingAction stopSnifferAction;
	private final ConfigureAction configureAction;
	private final CaptureFileAction captureAction;
	
	// The Model View toolbar contributions so we can add and remove them.
	private final IContributionItem snifferActionItem;
	private final IContributionItem stopSniffingActionItem;
	private final IContributionItem captureFileItem;
	private final ActionContributionItem configItem;

	private IToolBarManager toolbarManager;
	private IEventHandler probeEventHandler;
	private ISniffingDaemon currentSniffingDaemon;
	private IEventHandler changeHandler;
	
	SniffingActionManager(IToolBarManager manager) {
		toolbarManager = manager;
		
		captureAction = new CaptureFileAction(this);
		captureFileItem = new ActionContributionItem(captureAction);
		
		configureAction = new ConfigureAction(this);
		configItem = new ActionContributionItem(configureAction);
		
		startSnifferAction = new StartSnifferAction(this);
		snifferActionItem = new ActionContributionItem(startSnifferAction);
		
		stopSnifferAction = new StopSniffingAction(this);
		stopSniffingActionItem = new ActionContributionItem(stopSnifferAction);
		
		manager.add(stopSniffingActionItem);
		manager.add(snifferActionItem);
		manager.add(configItem);
		manager.add(captureFileItem);

		changeHandler = new IEventHandler() {
			public void handleEvent(IEvent event) {
				setState();				
			}
		};
		
		currentSniffingDaemon = Activator.getDefault().createSniffingDaemon(changeHandler);
		
		addProbeChangeListener();
	}
	
	private void addProbeChangeListener() {
		
		probeEventHandler = new IEventHandler() {
			public void handleEvent(IEvent event) {
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						currentSniffingDaemon = Activator.getDefault().createSniffingDaemon(changeHandler);
						setState();						
					}
				});
			}
		};
		Activator.getDefault().getProbeManager().addProbeChangeListener(probeEventHandler);
	}
	
	public void dispose() {
		Activator.getDefault().getProbeManager().removeProbeChangeListener(probeEventHandler);
	}
	
	public IToolBarManager getToolBar() {
		return toolbarManager;
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
		
		toolbarManager.insertAfter(CaptureFileAction.ID, snifferLabelItem);
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
		
		final ISniffingDaemon daemon = Activator.getDefault().getSniffingDaemon();

		if(daemon == null) {
			failAll("No sniffing service found");
			return;
		}
		
		final Collection<ICaptureInterface> interfaces = daemon.getInterfaces();
		
		if(interfaces == null) {
			failAll("No sniffing service found on remote probe");
			return;
		}
		
		if(interfaces.isEmpty()) {
			failLive("No packet capture interfaces found");
			if(daemon.isRunning())
				daemon.stop();
			return;
		}
			
		if(!hasEnabledInterface(interfaces)) {
			failLive("None of the interfaces are available");
			if(daemon.isRunning())
				daemon.stop();
			return;
		}
	
		if(daemon.isRunning()) {
			disableConfigAndCapture();
			startSnifferAction.setEnabled(false);
		}  else {
			stopSnifferAction.setEnabled(false);
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
		startSnifferAction.setEnabled(false);
		stopSnifferAction.setEnabled(false);
	}
	
	private boolean hasEnabledInterface(Collection<ICaptureInterface> interfaces) {
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
		stopSnifferAction.setEnabled(false);
		startSnifferAction.setEnabled(false);
		configureAction.setEnabled(false);
		captureAction.setEnabled(false);
	}
	
	private void enableAll() {
		stopSnifferAction.setEnabled(true);
		startSnifferAction.setEnabled(true);
		configureAction.setEnabled(true);
		captureAction.setEnabled(true);
	}
	
	public Point getConfigDialogLocation() {
		 Widget widget = configItem.getWidget();
		 if(!(widget instanceof ToolItem))
			 return null;
		 ToolItem item = (ToolItem) widget;
			 
		 int x = item.getBounds().x;
		 int y = item.getBounds().y + item.getBounds().height;
		 Point p = item.getDisplay().map(item.getParent(), null, x, y);
		 return p;
		 
	}
}
