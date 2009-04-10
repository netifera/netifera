package com.netifera.platform.ui.spaces.actions.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.netifera.platform.tools.options.BooleanOption;

public class BooleanOptionWidget extends OptionWidget {

	private final Button button;
	
	public BooleanOptionWidget(Composite parent, FormToolkit toolkit, BooleanOption option) {
		super(parent, toolkit, option);
		
		Composite area = toolkit.createComposite(parent);
		area.setLayout(new GridLayout(2,false));
		area.setToolTipText(option.getDescription());
		
//		Label label = toolkit.createLabel(area, option.getLabel());
//		label.setToolTipText(option.getDescription());

		button = toolkit.createButton(area, option.getLabel(), SWT.CHECK);
		button.setToolTipText(option.getDescription());
		button.setSelection(option.getValue());
		button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				setOptionValue();
				modified();
			}
			public void widgetSelected(SelectionEvent arg0) {
				setOptionValue();
				modified();
			}
		});
		
		if (option.isFixed())
			button.setGrayed(true);
	}
	
	public BooleanOption getOption() {
		return (BooleanOption) super.getOption();
	}

	public boolean isValid() {
		return true;
	}
	
	public void setOptionValue() {
		getOption().setValue(button.getSelection());
	}
}
