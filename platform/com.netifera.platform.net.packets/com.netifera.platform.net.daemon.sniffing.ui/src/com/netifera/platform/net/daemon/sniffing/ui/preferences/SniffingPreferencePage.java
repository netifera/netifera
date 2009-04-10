package com.netifera.platform.net.daemon.sniffing.ui.preferences;


import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import com.netifera.platform.net.daemon.sniffing.ui.Activator;
import com.netifera.platform.net.daemon.sniffing.ui.BackdoorSetup;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class SniffingPreferencePage
extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

	private Label status;
	private Button tryagain;
	public SniffingPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Network packet capture requires elevated privileges.");

	}

	private boolean isBackdoorInstalled() {

		/* get backdoor installation status trying to open an interface  */
		boolean backdoorInstalled = BackdoorSetup.isInstalled();

		if(!backdoorInstalled && getPreferenceStore().getBoolean(PreferenceConstants.P_USEBACKDOOR)) {
			/* change the preference value to false if the backdoor is not installed */
			getPreferenceStore().setValue(PreferenceConstants.P_USEBACKDOOR, backdoorInstalled);
		}
		return backdoorInstalled;
	}
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {

		status = new Label((Composite) getControl(),SWT.NONE);
		tryagain = new Button((Composite) getControl(),SWT.NONE);
		tryagain.setText("&Try Again");
		tryagain.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateBackdoorStatus();
				performApply(); // some users will expect Try Again to install the backdoor
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		tryagain.setToolTipText("Try to open network interface for packet capture");
		updateBackdoorStatus();

		addField(new BooleanFieldEditor(
				PreferenceConstants.P_USEBACKDOOR,"Use &backdoor to access network devices",getFieldEditorParent())
		);

	}

	private void updateBackdoorStatus() {
		if(isBackdoorInstalled()) {
			setStatus("Packet capture access obtained successfully",true);
		}
		else {
			setStatus("Couldn't obtain packet capture access",false);
		}

	}
	private void setStatus(String message,boolean good) {

		if(status.isDisposed()) {
			return;
		}

		status.setText(message);
		if(good) {
			status.setBackground(status.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			setErrorMessage(null);
		}
		else {
			status.setBackground(status.getDisplay().getSystemColor(SWT.COLOR_RED));
			setErrorMessage(message);
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performApply() {
		super.performApply();
		/* update the status after some seconds while sudo runs and the user fills the password*/
		status.setText("Checking ...");
		status.setBackground(status.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		setErrorMessage(null);

		Display.getDefault().timerExec(2500, new Runnable() {
			public void run() {
				updateBackdoorStatus();
			}
		});

		Display.getDefault().timerExec(6000, new Runnable() {
			public void run() {
				updateBackdoorStatus();
			}
		});
		

	}
	@Override
	public boolean performOk() {
		// TODO Auto-generated method stub
		boolean useBackdoor = getPreferenceStore().getBoolean(PreferenceConstants.P_USEBACKDOOR);
		boolean r = super.performOk();
		/* if preference changed changed change un/install backdoor */
		if((useBackdoor != getPreferenceStore().getBoolean(PreferenceConstants.P_USEBACKDOOR))) {
			BackdoorSetup.setInstall(!useBackdoor);
		}

		return r;
	}
}