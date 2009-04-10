package com.netifera.platform.model;

import com.db4o.ObjectContainer;
import com.netifera.platform.api.model.IWorkspace;

public interface IWorkspaceEx extends IWorkspace {
	ObjectContainer getRawDatabase();
}
