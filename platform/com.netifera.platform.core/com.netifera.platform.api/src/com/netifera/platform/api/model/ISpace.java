package com.netifera.platform.api.model;

import java.util.List;
import java.util.Set;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.api.tasks.ITaskStatus;

public interface ISpace {
	void open();
	void close();
	boolean isOpened();
	
	long getId();
	long getProbeId();
	String getName();
	void setName(String name);
	
	IWorkspace getWorkspace();
	
	List<IEntity> getEntities();
	int entityCount();
	IEntity getRootEntity();
	Set<String> getTags();
	
	void addEntity(IEntity entity);
	void updateEntity(IEntity entity);
	void removeEntity(IEntity entity);
	
	void addTaskRecord(ITaskStatus record);
	void updateTaskRecord(ITaskRecord record);

	void addChangeListener(IEventHandler handler);
	void addChangeListenerAndPopulate(IEventHandler handler);
	void removeChangeListener(IEventHandler handler);
	
	List<ITaskRecord> getTasks();
	void addTaskChangeListener(IEventHandler handler);
	void addTaskChangeListenerAndPopulate(IEventHandler handler);
	void removeTaskChangeListener(IEventHandler handler);
}
