package com.netifera.platform.ui.graphs.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JRootPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

abstract public class AWTEmbeddedControl extends Composite {
	protected Component embeddee; 
	
	public AWTEmbeddedControl(Composite parent) {
		super(parent, SWT.NO_BACKGROUND | SWT.BORDER | SWT.EMBEDDED);

		Frame locationFrame = SWT_AWT.new_Frame(this);

		Panel panel = new Panel(new BorderLayout()) {
			private static final long serialVersionUID = 3447368094074143729L;
			public void update(java.awt.Graphics g) {
				/* Do not erase the background. Doesn't seem to be necessary to reduce flicker */ 
				paint(g);
			}
		};
		locationFrame.add(panel);
		JRootPane root = new JRootPane();
		panel.add(root);
		Container contentPane = root.getContentPane();
	    
	    RGB backgroundColor = getBackground().getRGB();
	    locationFrame.setBackground(new java.awt.Color(backgroundColor.red, backgroundColor.green, backgroundColor.blue));
	    locationFrame.setIgnoreRepaint(false);
	    locationFrame.setVisible(true);

	    //locationFrame.add(ad); <- flicker
	    embeddee = createAWTComponent();
	    contentPane.add(embeddee);
	}

	abstract protected Component createAWTComponent();
}
