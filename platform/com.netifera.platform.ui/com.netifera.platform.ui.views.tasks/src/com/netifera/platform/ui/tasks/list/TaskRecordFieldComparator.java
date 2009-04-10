/**
 * 
 */
package com.netifera.platform.ui.tasks.list;

import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.util.AbstractFieldComparator;

/**
 * @author kevin
 *
 */
public class TaskRecordFieldComparator extends AbstractFieldComparator {
    // TODO enum here
	public final static int RUNSTATE = 0;
	public final static int NAME = 1;
	public final static int START_TIME = 2;
	public final static int ELAPSED_TIME = 3;
	
	protected AbstractFieldComparator nextFieldComparator;
	
	public TaskRecordFieldComparator() {
		this(null);
	}

	public TaskRecordFieldComparator(AbstractFieldComparator nextFieldComparator) {
		super();
		this.nextFieldComparator = nextFieldComparator;
		setSortBy(START_TIME);
		setAscending(false);
	}

	@Override
    public int compare(Object o1, Object o2) {
		int result = 0;
		
		if (((o1 instanceof ITaskRecord) == false) || ((o2 instanceof ITaskRecord) == false))
			return result;
		
		ITaskRecord task1 = (ITaskRecord) o1;
		ITaskRecord task2 = (ITaskRecord) o2;
		
		if(!(sortByField instanceof Integer)) {
			return result;
		}
		
		int sortBy = (Integer)sortByField;
		
		switch (sortBy) {
		case RUNSTATE:
			result = task1.getRunState() - task2.getRunState();
			break;
		case START_TIME:
			long time1 = task1.getStartTime();
			long time2 = task2.getStartTime();
			/* if not started (time == 0) consider future time */
			result = Long.valueOf(
					time1 == 0 ? Long.MAX_VALUE : time1).compareTo(
					time2 == 0 ? Long.MAX_VALUE : time2);
			break;
		case ELAPSED_TIME:
			result = Long.valueOf(task1.getElapsedTime()).compareTo(task2.getElapsedTime());
			break;
		/* for task name use a chained comparator, set it to ColumnViewerFieldComparator
		 * to get the task name string from the label provider. TaskRecord only have the
		 * class name. */
		case NAME:
		default:
			if(nextFieldComparator != null) {
				nextFieldComparator.setAscending(ascending);
				nextFieldComparator.setSortBy(sortByField);
				return nextFieldComparator.compare(o1, o2);
			}
		}
		return ascending ? result : -result;
	}
}
