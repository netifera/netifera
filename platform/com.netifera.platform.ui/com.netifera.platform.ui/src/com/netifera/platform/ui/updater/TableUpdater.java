package com.netifera.platform.ui.updater;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;

public class TableUpdater extends ControlUpdater {
	private final AbstractTableViewer tableViewer;
	private Object newInput;
	private Object currentInput;
	private volatile boolean refresh;
	private volatile boolean setInput;
	private volatile boolean clear;
	private boolean setItemCount;
	private int itemCount;
	private boolean autoScroll = true;
	
	//TODO: the use of ConcurrentMap seems pointless, we are using synchronized() locking, but
	// changing it to TreeMap or HashMap seems to be worst. investigate it with more time. 
	private final ConcurrentMap<Integer, Object> indexToElement = new ConcurrentHashMap<Integer, Object>();

	/* private constructor to force to use the get method */
	private TableUpdater(AbstractTableViewer tableViewer) {
		super(tableViewer.getControl());
		this.tableViewer = tableViewer;
		currentInput = tableViewer.getInput();
	}

	/*
	 * Updater must be created with this method to avoid creating more than one
	 * wrapper for the same viewer control
	 */
	public static TableUpdater get(AbstractTableViewer tableViewer) {
		ControlUpdater controlUpdater = ControlUpdater.get(tableViewer.getControl());

		/* return existing updater */
		if(controlUpdater instanceof TableUpdater) {
			return (TableUpdater)controlUpdater;
		}
		/* the existing updater is of different class */
		if(controlUpdater != null) {
			throw new IllegalArgumentException("The control has a registered updater of different class.");
		}
		/* create, register and return a new updater */
		return new TableUpdater(tableViewer);
	}

	/**
	 * updateControl() is synchronized and is executed in the UI thread, content
	 * providers calling TableUpdater methods will be blocked while this method
	 * executes. And the UI thread will be blocked while content providers
	 * invoke this updater methods below.
	 */
	protected void updateControl() {
		synchronized(this) {
			if(checkDisposed()) {
				return;
			}
			tableViewer.getControl().setRedraw(false);
			/* .clear() */
			if (clear) {
				itemCount = 0;
				indexToElement.clear();
				clear = false;
				tableViewer.setItemCount(itemCount);
			}
			/* .setInput() */
			if (setInput) {
				currentInput = newInput;
				newInput = null;
				setInput = false;
				tableViewer.setInput(currentInput);
			}

			/* .setItemCount() */
			if (setItemCount) {
				tableViewer.setItemCount(itemCount);
				if (autoScroll && (tableViewer instanceof TableViewer)) {
					Table table = ((TableViewer)tableViewer).getTable();
/*					int clientHeight = table.getClientArea().height;
					if (table.getHeaderVisible())
						clientHeight -= table.getHeaderHeight();
					int visibleCount = (clientHeight + table.getItemHeight() - 1)  / table.getItemHeight();
					if (visibleCount > 0)
						table.setTopIndex(itemCount - (itemCount < visibleCount ? itemCount : visibleCount));
*/					table.setTopIndex(itemCount - 1);
				}
				setItemCount = false;
			}

			/* .refresh() */
			if(refresh) {
				refresh = false;
				tableViewer.refresh();
			}
	
		/* ILazyContentProvider updateElement() */
		for (Entry<Integer, Object> e : indexToElement.entrySet()) {
			tableViewer.replace(e.getValue(), e.getKey());
			indexToElement.remove(e.getKey());
		}
		tableViewer.getControl().setRedraw(true);
		}/* synchronized block ends here */
	}

	/* the following methods are called from content providers */

	public synchronized void refresh() {
		refresh = true;
		indexToElement.clear();
		scheduleUpdate();
	}

	public void clear() {
		clear = true;
		scheduleUpdate();
	}

	public synchronized void setInput(Object input) {
		if (input == null || !input.equals(this.currentInput)) {
			indexToElement.clear();
			clear = true;
			newInput = input;
			setInput = true;
			refresh = true;//XXX necessary?
			scheduleUpdate();
		}
	}

	public  void replace(Object element, int index) {
		// XXX if newInput is set should ignore?
		if(element == null) {
			return;
		}
		indexToElement.put(index, element);
		scheduleUpdate();
	}

	public synchronized void setItemCount(int itemCount) {
		if(this.itemCount == itemCount) {
			return;
		}
		
		if (itemCount == 0) {
			indexToElement.clear();
		}
		
		this.itemCount = itemCount;
		setItemCount = true;
		scheduleUpdate();
	}

	public void setAutoScroll(boolean autoScroll) {
		this.autoScroll = autoScroll;
	}
	
	public boolean getAutoScroll() {
		return autoScroll;
	}
}
