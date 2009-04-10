package com.netifera.platform.api.tasks;

import java.util.ArrayList;
import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class TaskOutputMessage extends ProbeMessage implements ITaskOutputMessage {
	private static final long serialVersionUID = -2709486852883465617L;
	public static final String ID = "taskOutput";
	private List<ITaskOutput> taskOutput = new ArrayList<ITaskOutput>();
	
	public TaskOutputMessage() {
		super(ID);
	}
	
	public TaskOutputMessage(final ITaskOutput output) {
		super(ID);
		if(output != null) {
			addOutput(output);
		}
	}
	
	public void addOutput(final ITaskOutput output) {
		taskOutput.add(output);
	}
	
	public List<ITaskOutput> getTaskOutput() {
		return taskOutput;
	}
}
