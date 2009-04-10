package com.netifera.platform.ui.spaces.actions.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.netifera.platform.tools.options.IntegerOption;

public class IntegerOptionWidget extends OptionWidget {

	private final Text text;
	private final boolean hasMaximum;
	private final int maximumValue;
	
	public IntegerOptionWidget(Composite parent, FormToolkit toolkit, IntegerOption option) {
		super(parent, toolkit, option);
		if(option.hasMaximumValue()) {
			hasMaximum = true;
			maximumValue = option.getMaximumValue();
		} else {
			hasMaximum = false;
			maximumValue = -1;
		}
		
		Composite area = toolkit.createComposite(parent);
		area.setLayout(new GridLayout(2,false));
		area.setToolTipText(option.getDescription());
		
		Label label = toolkit.createLabel(area, option.getLabel());
		label.setToolTipText(option.getDescription());

		text = toolkit.createText(area, option.getValue().toString(), SWT.BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		text.setTextLimit(8);
		gd.widthHint = 100;
		text.setLayoutData(gd);
		text.setToolTipText(option.getDescription());

		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for(int i = 0; i < chars.length; i++) {
					if(!('0' <= chars[i] && chars[i] <= '9')) {
						e.doit = false;
						return;
					}
				}
				if(hasMaximum) {
					String s = getMergedText(e.text);
					try {
						long n = Long.parseLong(s);
						if(n > maximumValue) {
							e.doit = false;
							return;
						}
					} catch(NumberFormatException ex) {
						e.doit = false;
						return;
					}
				}
			}
			
			private String getMergedText(String newChars) {
				
				String s = text.getText();
				if(s.length() == 0)
					return newChars;
				
				Point p = text.getSelection();
				if(p.x != p.y) { // splice selection 
					return s.substring(0, p.x) + newChars + s.substring(p.y);
				}
				// insert at caret
				int caret = text.getCaretPosition();
				return s.substring(0, caret) + newChars + s.substring(caret);
			}
		});
		
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
	
	public IntegerOption getOption() {
		return (IntegerOption) super.getOption();
	}

	public boolean isValid() {
		try {
			return isValid(Integer.parseInt(text.getText()));
		} catch(NumberFormatException e) {
			return false;
		}
	}

	protected boolean isValid(int value) {
		return true;
	}

	protected int getValue() {
		return Integer.parseInt(text.getText());
	}
	
	public void setOptionValue() {
		getOption().setValue(Integer.parseInt(text.getText()));
	}
}
