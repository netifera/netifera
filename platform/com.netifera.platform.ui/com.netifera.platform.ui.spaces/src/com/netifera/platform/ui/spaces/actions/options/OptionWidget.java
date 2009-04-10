package com.netifera.platform.ui.spaces.actions.options;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.netifera.platform.api.tools.IOption;


public abstract class OptionWidget {
//	private final Composite parent;
//	private final FormToolkit toolkit;
	private final IOption option;
	
	public OptionWidget(Composite parent, FormToolkit toolkit, IOption option) {
//		this.parent = parent;
//		this.toolkit = toolkit;
		this.option = option;
	}

	public IOption getOption() {
		return option;
	}
	
	public abstract void setOptionValue();
	public abstract boolean isValid();
	
	protected void accept() { }
	protected void modified() { }
}
