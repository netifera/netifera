package com.netifera.platform.ui.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class OpenWorkspaceDialog extends Dialog {

	private Combo combo;
	private Label label;
	private Button browse;
	private String fileName;
	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public OpenWorkspaceDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * @return a string describing the path of the file selected or null. 
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Create contents of the dialog
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		parent.setLayout(new GridLayout(2,false));
		final GridData gdLabel = new GridData(SWT.LEFT, SWT.FILL, true, false);
		gdLabel.horizontalSpan = 2;
		label = new Label(container,SWT.NONE);
		label.setLayoutData(gdLabel);
		label.setText("Choose a workspace file to use for this session.\nIf the file doesn't exist a new workspace will be created.");
		browse = new Button(parent,SWT.NONE);

		browse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		browse.setText("Bro&wse...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(parent.getShell());
				final String file = fd.open();
				if(file.length() > 0) {
					combo.setText(file);
				}

			}
		}
		);
		combo = new Combo(container, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return container;
	}

	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		final GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
		gd.horizontalSpan = 2;
		parent.setLayoutData(gd);
		final Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				fileName = combo.getText();

			}
		});
		@SuppressWarnings("unused")
		final Button button_1 = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(500, 200);
	}
	@Override
	public int open() {
		// TODO Auto-generated method stub
		return super.open();
	}
}
