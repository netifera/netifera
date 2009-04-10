package com.netifera.platform.api.probe;

import java.util.List;

import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.IEntity;

public interface IProbeManagerService {
	IProbe getLocalProbe();
	IProbe createProbe(IEntity hostEntity, String name, String config, long spaceId);
	IProbe createProbe(String name, String config);
	void addProbeChangeListener(IEventHandler handler);
	void removeProbeChangeListener(IEventHandler handler);


	IProbe getProbeById(long probeId);
	List<IProbe> getProbeList();	

}
