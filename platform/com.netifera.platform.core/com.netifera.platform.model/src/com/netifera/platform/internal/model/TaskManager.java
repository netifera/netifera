package com.netifera.platform.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.db4o.ObjectContainer;
import com.db4o.query.Predicate;
import com.netifera.platform.api.tasks.ITaskRecord;

public class TaskManager {
	private final static boolean QUERY_DEBUG = false;
	private final ModelService model;
	private final ObjectContainer database;
	
	TaskManager(ObjectContainer database, ModelService model) {
		this.database = database;
		this.model = model;
	}
	
	public ITaskRecord findTaskById(long taskId) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findTaskById(" + taskId + ")");
		}
		
		final ITaskRecord task = lookupTaskById(taskId);
		
		if(task == null) {
			model.getLogger().debug("No task found for taskId(" + taskId + ")");
		}
		return task;
		
	}
	
	@SuppressWarnings("serial")
	private ITaskRecord lookupTaskById(final long taskId) {
		List<TaskRecord> results = database.query(new Predicate<TaskRecord>()  {
			public boolean match(TaskRecord candidate) {
				return candidate.getTaskId() == taskId;
			}
		});
		
		if(results.size() == 0) {
			return null;
		} else if (results.size() > 1){
			model.getLogger().error("Database corrupted, duplicate task found for id = " + taskId);
			return null;
		} else {
			return results.get(0);
		}
		
	}
	@SuppressWarnings("serial")
	public List<ITaskRecord> findTaskByProbeId(final long probeId) {
		if(QUERY_DEBUG) {
			model.getLogger().info("findTaskByProbeId(" + probeId + ")");
		}
		List<TaskRecord> results = database.query(new Predicate<TaskRecord>()  {
			public boolean match(TaskRecord candidate) {
				return candidate.getTaskId() == probeId;
			}
		});
		
		if(results.size() == 0) {
			model.getLogger().debug("No TaskRecord found for probeId = " + probeId);
			return Collections.emptyList();
		}
		return new ArrayList<ITaskRecord>(results);
	}
}
