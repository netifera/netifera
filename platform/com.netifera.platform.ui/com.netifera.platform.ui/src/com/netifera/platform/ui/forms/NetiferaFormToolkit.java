package com.netifera.platform.ui.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class NetiferaFormToolkit extends FormToolkit {

	public NetiferaFormToolkit(Display display) {
		super(display);
	}
	private BoldFontHolder boldFontHolder = new BoldFontHolder();

	public NetiferaExpand createZexpand(Composite parent,
			int expansionStyle) {
		NetiferaExpand ec = new NetiferaExpand(parent, getOrientation(),
				expansionStyle);
		ec.setMenu(parent.getMenu());
		adapt(ec, true, true);
		ec.setFont(boldFontHolder.getBoldFont(ec.getFont()));
		return ec;
	}
	/* copied from FormUtils */
	private static Font zFormUtilscreateBoldFont(Display display, Font regularFont) {
		FontData[] fontDatas = regularFont.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(fontDatas[i].getStyle() | SWT.BOLD);
		}
		return new Font(display, fontDatas);
	}

	private class BoldFontHolder {
		private Font normalFont;

		private Font boldFont;

		public BoldFontHolder() {
		}

		public Font getBoldFont(Font font) {
			createBoldFont(font);
			return boldFont;
		}

		private void createBoldFont(Font font) {
			if (normalFont == null || !normalFont.equals(font)) {
				normalFont = font;
				dispose();
			}
			if (boldFont == null) {
				boldFont = zFormUtilscreateBoldFont(getColors().getDisplay(),normalFont);
			}
		}

		public void dispose() {
			if (boldFont != null) {
				boldFont.dispose();
				boldFont = null;
			}
		}
	}

}

