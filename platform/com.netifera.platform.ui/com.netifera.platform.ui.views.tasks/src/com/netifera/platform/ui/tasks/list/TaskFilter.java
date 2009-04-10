/**
 * 
 */
package com.netifera.platform.ui.tasks.list;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.tasks.TaskStatus;

/**
 * @author kevin
 *
 */
public class TaskFilter extends ViewerFilter {
	private Map<Integer,Boolean> filterMap = new HashMap<Integer,Boolean>();
	private volatile boolean changed = false;
	private Viewer viewer;
	
	public void filter(int runState, boolean filter) {
		Boolean prev = filterMap.put(runState, filter);
		changed = (prev == null || prev != filter); 
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		this.viewer = viewer;
		if(element instanceof ITaskRecord) {
			int runState = ((ITaskRecord)element).getRunState();
			if(filterMap.containsKey(runState)) {
				return filterMap.get(runState);
			}
			return true;
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
		menuMgr.add(createAction("Running", TaskStatus.RUNNING));
		menuMgr.add(createAction("Waiting", TaskStatus.WAITING));
		menuMgr.add(createAction("Finished", TaskStatus.FINISHED));
		menuMgr.add(createAction("Failed", TaskStatus.FAILED));
		
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
		boolean checked = filterMap.containsKey(filterType) ? filterMap.get(filterType) : true;
		action.setChecked(checked);
		return action;
	}
}
