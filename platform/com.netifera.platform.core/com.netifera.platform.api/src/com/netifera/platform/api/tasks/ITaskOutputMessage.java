package com.netifera.platform.api.tasks;

import java.util.List;

public interface ITaskOutputMessage {

	void addOutput(ITaskOutput taskOutputMessage);

	List<ITaskOutput> getTaskOutput();

}