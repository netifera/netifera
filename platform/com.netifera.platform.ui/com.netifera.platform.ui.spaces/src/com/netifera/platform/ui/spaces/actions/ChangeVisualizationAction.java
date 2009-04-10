package com.netifera.platform.ui.spaces.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.spaces.editors.SpaceEditor;

public class ChangeVisualizationAction extends Action {
	private SpaceEditor editor;

	public ChangeVisualizationAction(SpaceEditor editor) {
		super("Change Visualization", SWT.DROP_DOWN);
		setImageDescriptor(Activator.getDefault().getImageCache().getDescriptor("icons/visualization.png"));
		
		this.editor = editor;
	}
	
	@Override
	public void run() {
        Menu menu = new Menu(Display.getDefault().getActiveShell(), SWT.POP_UP);
		for (final String name: Activator.getDefault().getVisualizationFactory().getVisualizationNames()) {
			MenuItem item = new MenuItem(menu, SWT.RADIO);
			item.setSelection(name.equals(editor.getVisualization()));
	        item.setText(name);
	        item.addListener(SWT.Selection, new Listener() {
	          public void handleEvent(Event e) {
					editor.setVisualization(name);
	          }
	        });
		}
        menu.setLocation(Display.getDefault().getCursorLocation());
        menu.setVisible(true);
	}
}
