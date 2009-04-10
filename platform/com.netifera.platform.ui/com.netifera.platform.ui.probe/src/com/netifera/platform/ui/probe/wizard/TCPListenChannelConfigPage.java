package com.netifera.platform.ui.probe.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.netifera.platform.util.patternmatching.InternetAddressMatcher;

public class TCPListenChannelConfigPage extends WizardPage {
	private Text ipAddressText;
	private Text portText;
	public TCPListenChannelConfigPage() {
		super("tcpListenConfig");
		setTitle("Configuration for TCP Listen Channel");
		setPageComplete(false);
	}
	
	public String getConfigString() {
		return "tcplisten:" + ipAddressText.getText() + ":"+ portText.getText();
	}
	
	public void createControl(Composite parent) {
		Composite container = createComposite(parent);
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				verifyFields();
			}
		};
		
		ipAddressText = createAddressField(container);
		ipAddressText.addModifyListener(listener);
		portText = createPortField(container);
		portText.addModifyListener(listener);
	}
	
	private void verifyFields() {
		final String addressString = ipAddressText.getText();
		final String portString = portText.getText();
		final boolean valid = addressValid(addressString) && portValid(portString);
		setPageComplete(valid);
	}
	
	private boolean addressValid(String address) {
		return InternetAddressMatcher.matches(address);
	}
	
	private boolean portValid(String portString) {
		try {
			final int port = Integer.parseInt(portString);
			return port > 0 && port <= 65535;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	private Composite createComposite(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
	    final GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 2;
	    c.setLayout(gridLayout);
	    setControl(c);
	    return c;
	}
	
	private Text createAddressField(Composite container) {
		createLabel(container, "Probe IP Address:");
		return createText(container, 16);
	}
	
	private Text createPortField(Composite container) {
		createLabel(container, "Probe Listen Port:");
		return createText(container,5);
	}
	
	private void createLabel(Composite container, String text) {
		final Label label = new Label(container, SWT.NONE);
		final GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
		label.setLayoutData(gd);
		label.setText(text);
	}
	
	private Text createText(Composite container, int limit) {
		final Text text = new Text(container, SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = 100;
		text.setLayoutData(gd);
		text.setTextLimit(limit);
		text.pack();
		return text;
	}

}
