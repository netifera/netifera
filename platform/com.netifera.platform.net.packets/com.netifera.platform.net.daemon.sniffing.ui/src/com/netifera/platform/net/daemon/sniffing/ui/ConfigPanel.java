package com.netifera.platform.net.daemon.sniffing.ui;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.netifera.platform.net.daemon.sniffing.ISniffingDaemon;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.pcap.ICaptureInterface;

public class ConfigPanel extends PopupDialog {

	private FormToolkit toolkit;
	private Form form;
	private Composite body;
	private ISniffingDaemon sniffingDaemon;
	private final SniffingActionManager manager;

	public ConfigPanel(Shell parent, SniffingActionManager manager) {
		super(parent, PopupDialog.INFOPOPUP_SHELLSTYLE | SWT.ON_TOP, true, false, false, false, false, null, "Press 'ESC' to exit");
		sniffingDaemon = Activator.getDefault().getSniffingDaemon();
		this.manager = manager;
		create();
		setHeader();
		addInterfaceSection();
		addModulesSection();
	}
	
	protected Point getInitialLocation(Point initialSize) {
		return manager.getConfigDialogLocation();
	}
	
	public int open() {
		manager.disableAll();
		return super.open();
	}
	
	public boolean close() {
		manager.setState();
		return super.close();
	}
	
	
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		
		composite.setLayout(new FillLayout());
		toolkit = new FormToolkit(composite.getDisplay());
		form = toolkit.createForm(composite);
		
		FormColors colors = toolkit.getColors();
		colors.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		body = form.getBody();
		body.setLayout(new GridLayout());
		toolkit.paintBordersFor(body);
		return composite;
	}
	
	private void setHeader() {
		form.setFont(JFaceResources.getDefaultFont());
		form.setText("Configure Sniffing Service");
		form.setSeparatorVisible(true);
		toolkit.decorateFormHeading(form);
		
	}
	
	private void addInterfaceSection() {
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION |
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		section.setText("Network Interfaces");
		section.setDescription("Select network interfaces to enable");
		
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		section.setClient(sectionClient);
		
		for(ICaptureInterface iface : sniffingDaemon.getInterfaces()) {
			addInterface(iface, sectionClient);
		}
		
	}
	
	private void addInterface(final ICaptureInterface iface, Composite parent) {
		final boolean enabled = sniffingDaemon.isEnabled(iface);
		final Button b = toolkit.createButton(parent, iface.toString(), SWT.CHECK);
		b.setSelection(enabled);
		b.setEnabled(iface.captureAvailable());
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sniffingDaemon.setEnabled(iface, b.getSelection());
			}
		});
	}
	
	private void addModulesSection() {
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION | Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED);
		section.setText("Sniffing Modules");
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		section.setClient(sectionClient);
		
		for(ISniffingModule module : sniffingDaemon.getModules()) {
			addModule(module, sectionClient);
		}
	}
	
	private void addModule(final ISniffingModule module, Composite parent) {
		final boolean enabled = sniffingDaemon.isEnabled(module);
		final Button b = toolkit.createButton(parent, module.getName(), SWT.CHECK);
		b.setSelection(enabled);
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				sniffingDaemon.setEnabled(module, b.getSelection());
			}
		});
	}
	protected void adjustBounds() {
		getShell().pack();
	}
}
