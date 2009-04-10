package com.netifera.platform.ui.application.workspaces;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LastPage extends WizardPage {
	
	LastPage() {
		super("lastPage");
		setTitle("Finish creating workspace");
	}

	public void createControl(Composite parent) {
		final Composite container = createComposite(parent);
	    createBanner(container, "Netifera will now restart with newly created workspace");
		setPageComplete(true);

		
		
	}
	private void createBanner(Composite container, String text) {
		final Label label = new Label(container, SWT.NONE);
		final GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.horizontalSpan = 2;		
		label.setLayoutData(gd);
		label.setText(text);
	}
	
	private Composite createComposite(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
	    final GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 2;
	    gridLayout.verticalSpacing = 20;
	    c.setLayout(gridLayout);
	    setControl(c);
	    return c;
	}

}
