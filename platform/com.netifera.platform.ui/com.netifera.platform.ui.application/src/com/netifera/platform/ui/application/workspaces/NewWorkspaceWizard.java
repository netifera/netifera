package com.netifera.platform.ui.application.workspaces;

import org.eclipse.jface.wizard.Wizard;

public class NewWorkspaceWizard extends Wizard {
	private WorkspaceNamePage firstPage;
	private LastPage lastPage;
	private WorkspaceRecord workspaceRecord;
	private boolean restart = true;
	public void addPages() {
		setWindowTitle("Create a new Workspace");
		firstPage = new WorkspaceNamePage();
		lastPage = new LastPage();
		addPage(firstPage);
		addPage(lastPage);
	}
	
	public boolean canFinish() {
		return getContainer().getCurrentPage() == lastPage;
	}
	
	@Override
	public boolean performFinish() {
		final String name = firstPage.getWorkspaceName();
		setWorkspaceRecord(WorkspaceChooser.createWorkspace(name, isRestart()));
		return true;
	}

	public void setWorkspaceRecord(WorkspaceRecord workspaceRecord) {
		this.workspaceRecord = workspaceRecord;
	}

	public WorkspaceRecord getWorkspaceRecord() {
		return workspaceRecord;
	}

	public void setRestart(boolean restart) {
		this.restart = restart;
	}

	public boolean isRestart() {
		return restart;
	}
}
