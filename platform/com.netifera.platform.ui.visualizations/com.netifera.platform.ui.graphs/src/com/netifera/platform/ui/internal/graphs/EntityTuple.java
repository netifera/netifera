package com.netifera.platform.ui.internal.graphs;

import java.util.Date;

import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.ui.spaces.graphs.SpaceGraphContentProvider;

public class EntityTuple implements Tuple {
	private IEntity entity;

	public EntityTuple(IEntity entity) {
		this.entity = entity;
	}
	
	public Table getTable() {
		return null;
	}

	public Schema getSchema() {
		return SpaceGraphContentProvider.getNodeSchema();
	}

	public boolean canGet(String field, Class fieldType) {
		return field.equals("entity") && fieldType.isAssignableFrom(IEntity.class);
	}

	public boolean canGetLong(String field) {
		return field.equals("realm");
	}

	public boolean canGetString(String field) {
		return field.equals("type") || field.equals("aggregate");
	}

	public Object get(String field) {
		if (field.equals("entity"))
			return entity;
/*		if (field.equals("type"))
			return entity.getTypeName();
		if (field.equals("realm"))
			return entity.getRealmId();
		if (field.equals("aggregate"))
			return entity.getTypeName();
*/		return null;
	}

	public Object get(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLong(String field) {
		if (field.equals("realm")) {
			return entity.getRealmId();
		}
		return 0;
	}

	public long getLong(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getRow() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	// ------------------------------------------------------------------
	// delegated to schema
	
	public int getColumnCount() {
		return getSchema().getColumnCount();
	}

	public int getColumnIndex(String field) {
		return getSchema().getColumnIndex(field);
	}

	public String getColumnName(int index) {
		return getSchema().getColumnName(index);
	}

	public Class getColumnType(String field) {
		return getSchema().getColumnType(field);
	}

	public Class getColumnType(int index) {
		return getSchema().getColumnType(index);
	}

	public Object getDefault(String field) {
		return getSchema().getDefault(field);
	}


	// --------------------------------------------------------------
	// unused

	public boolean canGetBoolean(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canGetDate(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canGetDouble(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canGetFloat(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canGetInt(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getBoolean(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getBoolean(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public Date getDate(String arg0) {
		return null;
	}

	public Date getDate(int arg0) {
		return null;
	}

	public double getDouble(String field) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getDouble(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFloat(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFloat(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getString(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public void revertToDefault(String arg0) {
		// TODO Auto-generated method stub
	}

	public boolean canSet(String arg0, Class arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetBoolean(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetDate(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetDouble(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetFloat(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetInt(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetLong(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canSetString(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void set(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	public void set(int arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setBoolean(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setBoolean(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setDate(String arg0, Date arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setDate(int arg0, Date arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setDouble(String arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setDouble(int arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setFloat(String arg0, float arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setFloat(int arg0, float arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setInt(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setInt(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setLong(String arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setLong(int arg0, long arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setString(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void setString(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
