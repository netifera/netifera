/**
 * 
 */
package com.netifera.platform.ui.tasks.output;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.netifera.platform.tasks.TaskLogOutput;

/**
 * @author kevin
 *
 */
public class TaskOutputFilter extends ViewerFilter {
	private long filterMap;
	private volatile boolean changed = false;
	private Viewer viewer;
	
	public void filter(int level, boolean filter) {
		long prev = filterMap;
		long mask = 1 << level;
		filterMap |= mask;
		filterMap -= filter ? mask : 0;
		changed = (filterMap != prev);
	}
	
	@Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
		this.viewer = viewer;
		if(element instanceof TaskLogOutput) {
		    TaskLogOutput msg = (TaskLogOutput)element;
		    //LogLevel level = ((TaskLogMessage)element).getLogLevel();
			//return((filterMap & 1<<level.ordinal()) == 0);
            return((filterMap & 1 << msg.getLogLevel()) == 0);
			}
		return true;
	}

	/* used to know if filter setting changed without adding a method and a cast 
	 * in general content provider code */
	@Override
    public boolean isFilterProperty(Object element, String property) {
		if(element == null && property == null) {
			return changed;
		}
		return false;
	}
	
	public void fillFilterMenu(IMenuManager menuMgr) {
        menuMgr.add(createAction("Debug",TaskLogOutput.DEBUG)); 
        menuMgr.add(createAction("Information",TaskLogOutput.INFO)); 
        menuMgr.add(createAction("Warning",TaskLogOutput.WARNING)); 
        menuMgr.add(createAction("Error",TaskLogOutput.ERROR));
	}

	private Action createAction(final String text, final int filterType) {
		Action action = new Action(text, Action.AS_CHECK_BOX) {
			@Override
            public void run() {
				filter(filterType, this.isChecked());
				if(viewer != null) {
					viewer.refresh();
				}
			}
		};
		boolean checked = ((filterMap & 1 << filterType) == 0); 
		action.setChecked(checked);
		return action;
	}
}
