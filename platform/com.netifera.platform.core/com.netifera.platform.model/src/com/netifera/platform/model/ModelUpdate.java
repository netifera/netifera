package com.netifera.platform.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.netifera.platform.api.dispatcher.ProbeMessage;

public class ModelUpdate extends ProbeMessage {
	
	private static final long serialVersionUID = -3267261510613222730L;
	public final static String ID = "RequestModelUpdate";
	private final List<IUpdateRecord> updateRecords;
	
	public ModelUpdate(List<IUpdateRecord> records) {
		super(ID);
		this.updateRecords = new ArrayList<IUpdateRecord>(records);
	}
	
	public ModelUpdate() {
		super(ID);
		updateRecords = new ArrayList<IUpdateRecord>();
	}
	
	public void addUpdate(IUpdateRecord update) {
		updateRecords.add(update);
	}
	
	public List<IUpdateRecord> getUpdateRecords() {
		return Collections.unmodifiableList(updateRecords);
	}

}
