package com.netifera.platform.ui.probe.status;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.probe.Activator;

public class ProbeStatusLine extends ControlContribution {

	public final static String ID = "com.netifera.ui.status.probe";
	private CLabel label;
	private IProbe probe;
	private IEventHandler probeChangeListener;
	
	private final static String PROBE_DISCONNECTED = "icons/probe_disconnected.png";
	private final static String PROBE_CONNECTING = "icons/probe_connecting.png";
	private final static String PROBE_CONNECTED = "icons/probe_connected.png";
	private final static String PROBE_FAILED = "icons/probe_failed.png";
	
	public ProbeStatusLine() {
		super(ID);
	}
	
	@Override
	protected Control createControl(Composite parent) {
		label = new CLabel(parent, SWT.SHADOW_NONE);
		label.setFont(JFaceResources.getDialogFont());
		label.setImage(Activator.getDefault().getImageCache().get(PROBE_DISCONNECTED));

		probeChangeListener = createProbeChangeListener(parent.getDisplay());
		
		label.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				Activator.getDefault().getProbeManager().removeProbeChangeListener(probeChangeListener);
			}
		});

		Activator.getDefault().getProbeManager().addProbeChangeListener(probeChangeListener);
		
		update();

		return label;
	}
	
	private IEventHandler createProbeChangeListener(final Display display) {
		return new IEventHandler() {
			public void handleEvent(IEvent event) {
				display.asyncExec(new Runnable() {
					public void run() {
						update();
					}
				});
			}
		};
	}
	
	@Override
	public void dispose() {
		Activator.getDefault().getProbeManager().removeProbeChangeListener(probeChangeListener);
		super.dispose();
	}

	public void setProbe(IProbe probe) {
		if (this.probe != probe) {
			this.probe = probe;
			update();
		}

/*
		System.out.println("force trim recreation");
			if (getParent() != null)
				getParent().update(true);
			((WorkbenchWindow)getWorkbenchWindow()).getTrimManager().forceLayout();
*///			((WorkbenchWindow)getWorkbenchWindow()).getTrimManager().
	}

	@Override
	public void update() {
		if (label == null)
			return;
		if (label.isDisposed())
			return;
		if (probe == null)
			return;
		label.setText(probe.getName());
		switch (probe.getConnectState()) {
		case DISCONNECTED:
			label.setImage(Activator.getDefault().getImageCache().get(PROBE_DISCONNECTED));
			label.setToolTipText("Disconnected");
			break;
		case CONNECTING:
			label.setImage(Activator.getDefault().getImageCache().get(PROBE_CONNECTING));
			label.setToolTipText("Connecting");
			break;
		case CONNECTED:
			label.setImage(Activator.getDefault().getImageCache().get(PROBE_CONNECTED));
			label.setToolTipText("Connected");
			break;
		case CONNECT_FAILED:
			label.setImage(Activator.getDefault().getImageCache().get(PROBE_FAILED));
			label.setToolTipText(probe.getConnectError());
		}
	}
}
