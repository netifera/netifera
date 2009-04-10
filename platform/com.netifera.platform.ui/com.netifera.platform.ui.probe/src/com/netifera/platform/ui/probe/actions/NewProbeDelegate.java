package com.netifera.platform.ui.probe.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;

import com.netifera.platform.ui.probe.wizard.NewProbeWizard;

public class NewProbeDelegate implements IViewActionDelegate {
	private IViewPart viewPart;
	public void init(IViewPart view) {
		viewPart = view;
	}

	public void run(IAction action) {
		NewProbeWizard wizard = new NewProbeWizard();
		WizardDialog dialog  = new WizardDialog(getWindow().getShell(), wizard);
		dialog.open();
	}
	private IWorkbenchWindow getWindow() {
		IWorkbenchPartSite site = viewPart.getSite();
		if(site == null) {
			return null;
		}
		return site.getWorkbenchWindow();
	}
	public void selectionChanged(IAction action, ISelection selection) {

	}
	

}
