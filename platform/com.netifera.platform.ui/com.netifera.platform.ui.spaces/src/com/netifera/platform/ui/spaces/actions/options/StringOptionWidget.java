package com.netifera.platform.ui.spaces.actions.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.netifera.platform.tools.options.StringOption;

public class StringOptionWidget extends OptionWidget {

	private final Text text;
	
	public StringOptionWidget(Composite parent, FormToolkit toolkit, StringOption option) {
		super(parent, toolkit, option);

		Composite area = toolkit.createComposite(parent);
		area.setLayout(new GridLayout(2,false));
		area.setToolTipText(option.getDescription());
		
		Label label = toolkit.createLabel(area, option.getLabel());
		label.setToolTipText(option.getDescription());

		text = toolkit.createText(area, option.getValue(), SWT.BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		gd.widthHint = 200;
//		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		text.setLayoutData(gd);
		text.setToolTipText(option.getDescription());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				modified();
			}
		});
		text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				modified();
				if (e.character == SWT.CR && isValid())
					accept();
			}
		});

		if (option.isFixed())
			text.setEnabled(false);
	}
	
	public StringOption getOption() {
		return (StringOption) super.getOption();
	}

	public boolean isValid() {
		return (getOption().allowsEmptyValue() || (text.getText().length() > 0)) && isValid(text.getText());
	}

	public boolean isValid(String value) {
		return true;
	}

	protected String getValue() {
		return text.getText();
	}
	
	public void setOptionValue() {
		getOption().setValue(text.getText());
	}
}
