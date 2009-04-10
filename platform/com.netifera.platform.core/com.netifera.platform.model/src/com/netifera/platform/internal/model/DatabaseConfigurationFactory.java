package com.netifera.platform.internal.model;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
import com.db4o.diagnostic.DiagnosticToConsole;
import com.db4o.reflect.jdk.JdkReflector;
import com.netifera.platform.api.model.AbstractEntity;

public class DatabaseConfigurationFactory {
	final private static int GLOBAL_ACTIVATION_DEPTH = 10;
	//final private static int GLOBAL_UPDATE_DEPTH = 0;
	final private static boolean DIAGNOSTICS_ENABLED = false;
	final private static boolean DEBUG_OUTPUT_ENABLED = false;
	final private static int DEBUG_OUTPUT_LEVEL = 4;
	
	//final private Db4oService db4oService;
	
	
	//DatabaseConfigurationFactory(Db4oService db4oService) {
	//	this.db4oService = db4oService;
	//}
	DatabaseConfigurationFactory() {
		
	}
	
	public Configuration createDefaultConfiguration() {
		//Configuration config = db4oService.newConfiguration();
		Configuration config = Db4o.newConfiguration();
		config.reflectWith(new JdkReflector(this.getClass().getClassLoader()));
		// make indexes for commonly queried fields
		config.objectClass(AbstractEntity.class).objectField("id").indexed(true);
		config.objectClass(AbstractEntity.class).objectField("realmId").indexed(true);
		config.objectClass(AbstractEntity.class).objectField("queryKey").indexed(true);
		config.objectClass(TaskRecord.class).objectField("taskId").indexed(true);
		
		// save entity references
		config.objectClass(AbstractEntity.class).cascadeOnUpdate(true);
		
		config.activationDepth(GLOBAL_ACTIVATION_DEPTH);
		//config.updateDepth(GLOBAL_UPDATE_DEPTH);
		if(DIAGNOSTICS_ENABLED) {
			config.diagnostic().addListener(new DiagnosticToConsole());
		}
		if(DEBUG_OUTPUT_ENABLED) {
			config.messageLevel(DEBUG_OUTPUT_LEVEL);
		}
		return config;
	}
	
	public ObjectContainer openContainer(String path) {
		Configuration config = createDefaultConfiguration();
		return Db4o.openFile(config, path).ext();
	}
	
	public ObjectContainer openContainer(Configuration config, String path) {
		return Db4o.openFile(config, path).ext();
	}
}
