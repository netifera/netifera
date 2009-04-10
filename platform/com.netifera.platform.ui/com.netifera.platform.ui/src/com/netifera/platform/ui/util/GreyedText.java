package com.netifera.platform.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class GreyedText {
	private final String text;
	private final Text textControl;
	private final Color foregroundColor;
	private volatile boolean isShowingGrey = false;
	public GreyedText(Text textControl, String text) {
		this.text = text;
		this.textControl = textControl;
		foregroundColor = textControl.getForeground();
		
		addListeners();
		showGreyedText();
	}

	private void addListeners() {
		textControl.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				hideGreyedText();
			}

			public void focusLost(FocusEvent e) {
				if(!isShowingGrey)
				showGreyedText();
			}
		}
		);
		textControl.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				if(!isShowingGrey && !textControl.isFocusControl())
				showGreyedText();
				else if(isShowingGrey)
					isShowingGrey = false;
			}				
		}
		);
	}
	
	public void showGreyedText() {
		
		/* only show the grayed text if the control is empty */
		if(textControl.getText().length() > 0) {
			return;
		}

		textControl.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		setText(text);
		isShowingGrey = true;
	}
	
	private void setText(String text) {
		final boolean showValue = isShowingGrey;
		/* avoid the Modify handler to call this method again */
		isShowingGrey = true;
		textControl.setText(text);
		isShowingGrey = showValue;
	}
	
	public void hideGreyedText() {
		if(textControl.getText().equals(text)) {
			setText("");
			textControl.setForeground(foregroundColor);
			isShowingGrey = false;
		}
	}
}
