package com.netifera.platform.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.netifera.platform.ui.UIPlugin;

public class BalloonManager {
	private BalloonWindow balloon;
	private static final int CLOSE_DELAY = 2000;
	
	public void info(String text) {
		message(UIPlugin.getPlugin().getImageCache().get("icons/message_info.png"), text);
	}
	
	public void warning(String text) {
		message(UIPlugin.getPlugin().getImageCache().get("icons/message_warning.png"), text);
	}
	
	public void error(String text) {
		message(UIPlugin.getPlugin().getImageCache().get("icons/message_error.png"), text);
	}
	
	public void message(final Image image, final String text) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				
				if (balloon != null) {
					try {
						balloon.close();
					} 
					catch(Exception e) {};
				}
				balloon = new BalloonWindow(display, SWT.ON_TOP | SWT.TITLE /*| SWT.CLOSE | SWT.RIGHT | SWT.BOTTOM*/);
				Rectangle bounds = display.getBounds();
				balloon.setLocation(bounds.x+bounds.width,bounds.y+bounds.height-48);
				if(image != null) {
					balloon.setImage(image);
				}
				balloon.setText(text);
				balloon.open();
				
				/* schedule balloon hide */
				display.timerExec(CLOSE_DELAY, new Runnable() {
					public void run() {
						try {
							balloon.close();
							balloon = null;
							} catch(Exception e) {};
					}
				});
			}
		});
	}
}
