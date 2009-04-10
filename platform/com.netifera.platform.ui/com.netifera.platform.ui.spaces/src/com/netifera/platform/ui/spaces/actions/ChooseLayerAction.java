package com.netifera.platform.ui.spaces.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.netifera.platform.api.model.layers.ILayerProvider;

public abstract class ChooseLayerAction extends Action {
	
	public ChooseLayerAction(String text, ImageDescriptor icon) {
		super(text, SWT.DROP_DOWN);
		setImageDescriptor(icon);
	}
	
	@Override
	public void run() {
        Menu menu = new Menu(Display.getDefault().getActiveShell(), SWT.POP_UP);
		for (final ILayerProvider provider: getLayers()) {
			if (provider.getLayerName() == null)
				continue;
			MenuItem item = new MenuItem(menu, SWT.RADIO);
			item.setSelection(getActiveLayer() == provider);
	        item.setText(provider.getLayerName());
	        item.addListener(SWT.Selection, new Listener() {
	          public void handleEvent(Event e) {
	        	  if (getActiveLayer() == provider) {
	        		  setActiveLayer(null);
	        	  } else {
	        		  setActiveLayer(provider);
	        	  }
	          }
	        });
		}
        menu.setLocation(Display.getDefault().getCursorLocation());
        menu.setVisible(true);
	}
	
	protected abstract List<ILayerProvider> getLayers();
	protected abstract ILayerProvider getActiveLayer();
	protected abstract void setActiveLayer(ILayerProvider provider);
}
