package com.netifera.platform.ui.internal.tasks.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

public class ProgressBarStack extends Composite {
	
	private final ProgressBar progressBar;
	private final ProgressBar indeterminateProgressBar;
	private final StackLayout stackLayout;
	public ProgressBarStack(Composite parent) {
		super(parent, SWT.NONE);
		
		stackLayout = new StackLayout();
		setLayout(stackLayout);
		indeterminateProgressBar = new ProgressBar(this, SWT.INDETERMINATE);
		progressBar = new ProgressBar(this, SWT.NONE);
		stackLayout.topControl = indeterminateProgressBar;
		
	}

	
	public void setSelection(int value) {
		if(isIndeterminate()) {
			focusDeterminateProgress();
		}
		if(value > 100) value = 100;
		if(value < 0 ) value = 0;
		progressBar.setSelection(value);
		progressBar.setToolTipText(value + "%");
	}
	
	private boolean isIndeterminate() {
		return stackLayout.topControl  == indeterminateProgressBar;
	}
	
	private void focusDeterminateProgress() {
		stackLayout.topControl = progressBar;
		layout();
	}
}
