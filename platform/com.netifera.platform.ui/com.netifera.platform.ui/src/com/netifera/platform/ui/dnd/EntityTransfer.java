package com.netifera.platform.ui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.model.IWorkspace;

public class EntityTransfer extends ByteArrayTransfer {
	
	/**
	 * Model instance is used to retrieve entity instances
	 * by model id when deserializing the transfer.
	 */
	IModelService model;
	
	/**
	 * Singleton instance.
	 */
	private static EntityTransfer instance = new EntityTransfer();
	
	private static final String TYPE_NAME = "entity-transfer-format";
	private static final int TYPEID = registerType(TYPE_NAME);
	
	/**
	 * Returns the singleton instance.
	 * @return The singleton instance.
	 */
	public static EntityTransfer getInstance() {
		return instance;
	}
	
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	
	/* This is mostly copied from ResourceTransfer */
	protected void javaToNative(Object data, TransferData transferData) {
		System.out.println("j2n "+data);
		if(!(data instanceof IEntity[])) {
			return;
		}
		IEntity[] entities = (IEntity[]) data;
		
		int entityCount = entities.length;
		System.out.println("  "+entityCount+" entities");
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(out);
			dataOut.writeInt(entityCount);
			
			for(IEntity e : entities) {
				dataOut.writeLong(e.getId());
			}
			
			dataOut.close();
			out.close();
			byte[] bytes = out.toByteArray();
			super.javaToNative(bytes, transferData);
			System.out.println("  done!");
		} catch (IOException e) {
			// Just ignore, and nothing will be sent
		}
		System.out.println("  done");
	}
	
	protected Object nativeToJava(TransferData transferData) {
		if(model == null || model.getCurrentWorkspace() == null) {
			throw new RuntimeException("Cannot deserialize entity because model is not bound or no workspace open in EntityTransfer");
		}
		
		final IWorkspace workspace = model.getCurrentWorkspace();
		
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		if(bytes == null) {
			return null;
		}
		DataInputStream in = new DataInputStream(
				new ByteArrayInputStream(bytes));
		try {
			int count = in.readInt();
			IEntity[] entities = new IEntity[count];
			for(int i = 0; i < count; i++) {
				long id = in.readLong();
				entities[i] = workspace.findById(id);
			}
			return entities;
		} catch(IOException e) {
			return null;
		}
	}
	
	/*
	 * This is a bit confusing and probably the wrong way
	 * to do this but there are two instances of this class.
	 * The OSGi component instance, and the singleton instance. 
	 * The model is set in the singleton instance through the
	 * component binding methods.
	 */
	protected void setModelService(IModelService model) {
		getInstance().model = model;	
	}
	
	protected void unsetModelService(IModelService model) {
		getInstance().model = null;
	}
	
	

}
