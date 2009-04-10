package com.netifera.platform.ui.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AskPasswordDialog extends Dialog
{
	String password;
	private Text passTxt;
	private Label passLabel;
	private Label headLabel;
	public AskPasswordDialog(Shell parentShell) {
		super(parentShell);
	}
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(2,false));
		headLabel = new Label(c,SWT.NONE);
		headLabel.setText("Enter your password to run a task with administrative privileges.");
		GridDataFactory.generate(headLabel,2,1);
		
		passLabel = new Label(c,SWT.NONE);
		passLabel.setText("Password:");
		
		passTxt = new Text(c,SWT.NONE);
		passTxt.setEchoChar('*');
		GridDataFactory.generate(passTxt,1,1);
		return c;
	}
	@Override
	protected void okPressed() {
		password = passTxt.getText();
		setReturnCode(OK);
		close();
	}
	
	public String getPassword() {
		return password;
	}
}