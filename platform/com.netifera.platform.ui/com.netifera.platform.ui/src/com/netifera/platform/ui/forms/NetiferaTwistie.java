package com.netifera.platform.ui.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ToggleHyperlink;

/**
 * Cloned Twistie class to add access to the hover flag. Could be removed if the
 * RCP form toolkit is updated. This is required by the cloned NetiferaExpand
 */
public class NetiferaTwistie extends ToggleHyperlink {
	private static final int[] onPoints = { 0, 2, 8, 2, 4, 6 };

	private static final int[] offPoints = { 2, -1, 2, 8, 6, 4 };

	/**
	 * Creates a control in a provided composite.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */
	public NetiferaTwistie(Composite parent, int style) {
		super(parent, style);
		innerWidth = 9;
		innerHeight = 9;
	}

	/*
	 * @see SelectableControl#paint(GC)
	 */
	protected void paintHyperlink(GC gc) {
		Color bg;
		if (!isEnabled())
			bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		else if (hover && getHoverDecorationColor() != null)
			bg = getHoverDecorationColor();
		else if (getDecorationColor() != null)
			bg = getDecorationColor();
		else
			bg = getForeground();
		gc.setBackground(bg);
		int[] data;
		Point size = getSize();
		int x = (size.x - 9) / 2;
		int y = (size.y - 9) / 2;
		if (isExpanded())
			data = translate(onPoints, x, y);
		else
			data = translate(offPoints, x, y);
		gc.fillPolygon(data);
		gc.setBackground(getBackground());
	}

	private int[] translate(int[] data, int x, int y) {
		int[] target = new int[data.length];
		for (int i = 0; i < data.length; i += 2) {
			target[i] = data[i] + x;
		}
		for (int i = 1; i < data.length; i += 2) {
			target[i] = data[i] + y;
		}
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		redraw();
	}
	public boolean isHover() {
		return hover;
	}
	
	public void setHover(boolean hover) {
		this.hover = hover;
	}
	
}
