package com.netifera.platform.ui.probe.wizard;

import org.eclipse.jface.wizard.Wizard;

import com.netifera.platform.ui.probe.Activator;

public class NewProbeWizard extends Wizard {

	private FirstPage firstPage;
	private TCPListenChannelConfigPage tcpListenPage;
	@Override
	public void addPages() {
		
		setWindowTitle("Create a new Probe");
		firstPage = new FirstPage();
		tcpListenPage = new TCPListenChannelConfigPage();
		addPage(firstPage);
		addPage(tcpListenPage);
	}
	@Override
	public boolean performFinish() {
		final String name = firstPage.getName();
		final String config = tcpListenPage.getConfigString();
		Activator.getDefault().getProbeManager().createProbe(name, config);
		return true;
	}

}
