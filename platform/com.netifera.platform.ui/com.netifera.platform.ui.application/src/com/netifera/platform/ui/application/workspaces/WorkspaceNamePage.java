package com.netifera.platform.ui.application.workspaces;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class WorkspaceNamePage extends WizardPage {

	private Text workspaceNameText;
	
	WorkspaceNamePage() {
		super("firstPage");
		setTitle("Workspace Name");
		setPageComplete(false);
	}
	public void createControl(Composite parent) {
		final Composite container = createComposite(parent);
	    createBanner(container, "Select a name for your new workspace");
	
	    workspaceNameText = createWorkspaceName(container);
	}
	
	
	public String getWorkspaceName() {
		return workspaceNameText.getText();
	}
	private Text createWorkspaceName(Composite container) {
		createLabel(container, "Workspace Name:");
		final Text t = createText(container, 16);
		t.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if(t.getText().length() > 0) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
				
			}
	
		});
		return t;
	}
	
	
	private void createBanner(Composite container, String text) {
		final Label label = new Label(container, SWT.NONE);
		final GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gd.horizontalSpan = 2;		
		label.setLayoutData(gd);
		label.setText(text);
	}
	private void createLabel(Composite container, String text) {
		final Label label = new Label(container, SWT.NONE);
		final GridData gd = new GridData(SWT.END, SWT.CENTER, false, false);
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
	private Text createText(Composite container, int limit) {
		final Text text = new Text(container, SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd.widthHint = 10 * limit;
		text.setLayoutData(gd);
		text.setTextLimit(limit);
		return text;
	}

}
