package com.netifera.platform.net.internal.daemon.sniffing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.service.component.ComponentContext;

import com.netifera.platform.api.dispatcher.DispatchException;
import com.netifera.platform.api.dispatcher.DispatchMismatchException;
import com.netifera.platform.api.dispatcher.IMessageDispatcher;
import com.netifera.platform.api.dispatcher.IMessageDispatcherService;
import com.netifera.platform.api.dispatcher.IMessageHandler;
import com.netifera.platform.api.dispatcher.IMessenger;
import com.netifera.platform.api.dispatcher.IProbeMessage;
import com.netifera.platform.api.dispatcher.MessengerException;
import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.net.daemon.sniffing.ISniffingDaemonEx;
import com.netifera.platform.net.daemon.sniffing.ISniffingModule;
import com.netifera.platform.net.daemon.sniffing.model.CaptureFileEntity;
import com.netifera.platform.net.daemon.sniffing.model.ISniffingEntityFactory;
import com.netifera.platform.net.daemon.sniffing.model.NetworkInterfaceEntity;
import com.netifera.platform.net.daemon.sniffing.model.SniffingSessionEntity;
import com.netifera.platform.net.internal.daemon.probe.CancelCaptureFile;
import com.netifera.platform.net.internal.daemon.probe.CaptureFileValid;
import com.netifera.platform.net.internal.daemon.probe.InterfaceRecord;
import com.netifera.platform.net.internal.daemon.probe.ModuleRecord;
import com.netifera.platform.net.internal.daemon.probe.RequestInterfaceInformation;
import com.netifera.platform.net.internal.daemon.probe.RequestModuleInformation;
import com.netifera.platform.net.internal.daemon.probe.RunCaptureFile;
import com.netifera.platform.net.internal.daemon.probe.SetInterfaceEnableState;
import com.netifera.platform.net.internal.daemon.probe.SetModuleEnableState;
import com.netifera.platform.net.internal.daemon.probe.SniffingDaemonStatus;
import com.netifera.platform.net.internal.daemon.probe.SniffingModuleOutput;
import com.netifera.platform.net.internal.daemon.probe.StartSniffingDaemon;
import com.netifera.platform.net.internal.daemon.probe.StopSniffingDaemon;
import com.netifera.platform.net.pcap.ICaptureInterface;
import com.netifera.platform.net.sniffing.ICaptureFileInterface;
import com.netifera.platform.net.sniffing.ISniffingEngineService;
import com.netifera.platform.net.sniffing.util.ICaptureFileProgress;

public class SniffingDaemon implements ISniffingDaemonEx, ISniffingModuleOutput {
	
	/*
	 * All sniffing modules which have been registered with the daemon.
	 */
	private final Set<ISniffingModule> modules;
	
	/*
	 * Sniffing modules which have been enabled with the enableModules()
	 * method.
	 */
	private final Set<EnabledSniffingModule> enabledModules;
	
	/*
	 * Network interfaces which have been enabled with the enableInterfaces()
	 * method.
	 */
	private final Set<ICaptureInterface> enabledInterfaces;	
	
	private final Map<String, ISniffingModule> moduleByName = new HashMap<String, ISniffingModule>();
	private Thread captureFileThread;

	private IMessenger openMessenger;

	/*
	 * DS controlled services
	 */
	private ISniffingEngineService defaultSniffingEngine;
	private ISniffingEntityFactory sniffingEntityFactory;
	private IProbeManagerService probeManager;
	private ILogger logger;
	/*
	 * Flag which indicates when the sniffing daemon is currently running.
	 */
	private boolean running;
	private boolean isActivated;
	
	private ICaptureFileInterface runningCaptureFile;
	
	/**
	 * SniffingDaemon constructor creates a TreeSet for storing sniffing modules
	 * in alphabetic order.
	 */
	public SniffingDaemon() {
		modules = new TreeSet<ISniffingModule>(new Comparator<ISniffingModule>() {

			public int compare(ISniffingModule module1,
					ISniffingModule module2) {
				return module1.getName().compareTo(module2.getName());
			}
			
		});
		
		enabledModules = new HashSet<EnabledSniffingModule>();
		enabledInterfaces = new HashSet<ICaptureInterface>();
		running = false;
		
	}
	
	public Set<ISniffingModule> getModules() {
		return Collections.unmodifiableSet(modules);
	}
	
	
	public Collection<ICaptureInterface> getInterfaces() {
		return defaultSniffingEngine.getInterfaces();
	}
	
	public boolean isEnabled(ICaptureInterface iface) {
		return enabledInterfaces.contains(iface);
	}
	
	public boolean hasEnabledInterfaces() {
		return !enabledInterfaces.isEmpty();
	}
	
	public void disableAllInterfaces() {
		System.out.println("dispable all interfaces");
		enabledInterfaces.clear();
	}
	
	public void setEnabled(ICaptureInterface iface, boolean enable) {
		if(running) {
			throw new IllegalStateException("Cannot configure interfaces while daemon is running");
		}
		
		if(!getInterfaces().contains(iface)) {
			throw new IllegalArgumentException("Unknown interface passed to setEnabled()");
		}
		
		if(!iface.captureAvailable()) {
			throw new IllegalArgumentException("Cannot enable unavailable interface");
		}
		
		if(enable) {
			enabledInterfaces.add(iface);
		} else {
			enabledInterfaces.remove(iface);
		}
	}
	
	public boolean isEnabled(ISniffingModule module) {
		return findEnabledModule(module) != null;
	}
	
	public void setEnabled(ISniffingModule module, boolean enable) {
		if(running) {
			throw new IllegalStateException("Cannot configure modules while daemon is running");
		}
		if(!getModules().contains(module)) {
			throw new IllegalArgumentException("Unknown module passed to setEnabled()");
		}
		if(enable) {
			if(findEnabledModule(module) == null) {
				enabledModules.add(new EnabledSniffingModule(module, this, logger));
			}
		} else {
			EnabledSniffingModule esm = findEnabledModule(module);
			if(esm != null) {
				enabledModules.remove(esm);
			}
		}
	}
	
	private EnabledSniffingModule findEnabledModule(ISniffingModule module) {
		for(EnabledSniffingModule m : enabledModules) {
			if(m.getModule().equals(module)) {
				return m;
			}
		}
		return null;
	}
	
	public void enableAllModules() {
		enableModules(getModules());
	}
	
	public synchronized void enableModules(Set<ISniffingModule> enabledModuleSet) {
		if(running) {
			throw new IllegalStateException("Cannot enable modules while daemon is running");
		}
		
		if(enabledModuleSet.isEmpty()) {
			throw new IllegalArgumentException("enableModules() called with empty set");
		}
		
		enabledModules.clear();
		for(ISniffingModule module : enabledModuleSet) {
			if(!modules.contains(module)) {
				throw new IllegalArgumentException("Unknown Sniffing Module requested in enableModules() " +
						module);
			}
			enabledModules.add(new EnabledSniffingModule(module, this, logger));
		}
	}
	
	public void enableAllInterfaces() {
		enableInterfaces(getInterfaces());
	}
	
	public synchronized void enableInterfaces(Collection<ICaptureInterface> interfaces) {
		if(running) {
			throw new IllegalStateException("Cannot enable interfaces while daemon is running");
		}
		
		if(interfaces.isEmpty()) {
			throw new IllegalArgumentException("enableInterfaces() called with empty collection");
		}
		enabledInterfaces.clear();
		for(ICaptureInterface iface : interfaces) {
			if(!iface.captureAvailable()) {
				continue;
			}
			if(!getInterfaces().contains(iface)) {
				throw new IllegalArgumentException("Unknown network interface requested in enableInterfaces() "
					+ iface);
			}
			enabledInterfaces.add(iface);
		}
	}
	
	public ICaptureFileInterface createCaptureFileInterface(String path) {
		return defaultSniffingEngine.createCaptureFileInterface(path);
	}
	
	public synchronized void runCaptureFile(long spaceId, ICaptureFileInterface iface, final ICaptureFileProgress progress) {
		
		if(!iface.isValid()) {
			progress.error("Invalid interface " + iface.getName(), null);
			return;
		}
		final long realm = probeManager.getLocalProbe().getEntity().getId();
		final CaptureFileEntity captureFile = sniffingEntityFactory.createCaptureFile(realm, spaceId, iface.getPath());
		
		
		final Set<SniffingDaemonInterface> interfaces = new HashSet<SniffingDaemonInterface>();

		interfaces.add(new SniffingDaemonInterface(iface, captureFile.getId()));
		
		for(EnabledSniffingModule module : enabledModules) {
			module.start(defaultSniffingEngine, interfaces, spaceId);
		}
		
		running = true;
		runningCaptureFile = iface;
		iface.process(new ICaptureFileProgress() {

			public void error(String message, Throwable e) {
				stop();
				progress.error(message, e);
			}

			public void finished() {
				runningCaptureFile = null;
				stop();
				progress.finished();
			}

			public boolean updateProgress(int percent, int count) {
				return progress.updateProgress(percent, count);
			}
		});
		
	}
	
	public void cancelCaptureFile() {
		if(runningCaptureFile != null) runningCaptureFile.cancelProcessing();
	}
	
	public void start(long spaceId, long realm) {
		start(enabledInterfaces, defaultSniffingEngine, spaceId, realm);
	}
	
	public void start(long spaceId) {
		if(running) {
			return;
		}
		
		final long realm = probeManager.getLocalProbe().getEntity().getId();
		final SniffingSessionEntity session = sniffingEntityFactory.createSniffingSession(realm, spaceId);
		start(spaceId, session.getId());
		
	}
	
	public synchronized void start(Set<ICaptureInterface> interfaces, ISniffingEngineService sniffingEngine, long spaceId, long realm) {
		if(running)
			return;
		final Set<SniffingDaemonInterface> ifs = new HashSet<SniffingDaemonInterface>();
		for(ICaptureInterface iface : interfaces) {
			NetworkInterfaceEntity interfaceEntity = sniffingEntityFactory.createNetworkInterface(realm, spaceId, iface.getName());
			ifs.add(new SniffingDaemonInterface(iface, interfaceEntity.getId()));
		}
		for(EnabledSniffingModule module : enabledModules) {
			module.start(sniffingEngine, ifs, spaceId);
		}
		running = true;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.netifera.platform.api.daemon.IDaemon#stop()
	 */
	public synchronized void stop() {
		if(!running) {
			return;
		}
		for(EnabledSniffingModule module : enabledModules) {
			module.stop();
		}
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setOpenMessenger(IMessenger messenger) {
		openMessenger = messenger;
	}
	
	public void printOutput(String message) {
		if(openMessenger == null) return;
		if (message.endsWith("\n")) {
			message = message.concat("\n");
		}
		try {
			openMessenger.emitMessage(new SniffingModuleOutput(message));
		} catch (MessengerException e) {
			openMessenger = null;
		}
	}
	
	protected void activate(ComponentContext ctx) {
		synchronized(modules) {
			isActivated = true;
			enableAllModules();
			enableAllInterfaces();
		}
	}
	
	protected void deactivate(ComponentContext ctx) {
		
	}
	
	/*
	 * OSGi DS bindings
	 */
	protected void registerModule(ISniffingModule module) {
		synchronized(modules) {
			modules.add(module);
			moduleByName.put(module.getName(), module);
			if(isActivated)
				setEnabled(module, true);
		}
	}
	
	protected void unregisterModule(ISniffingModule module) {
		synchronized(modules) {
			modules.remove(module);
			moduleByName.remove(module.getName());
		}
	}
	
	protected void setSniffingEngine(ISniffingEngineService sniffingEngine) {
		this.defaultSniffingEngine = sniffingEngine;
	}
	
	protected void unsetSniffingEngine(ISniffingEngineService sniffingEngine) {
	}
	
	protected void setEntityFactory(ISniffingEntityFactory factory) {
		this.sniffingEntityFactory = factory;
	}
	
	protected void unsetEntityFactory(ISniffingEntityFactory factory) {
		
	}
	
	protected void setProbeManager(IProbeManagerService manager) {
		this.probeManager = manager;
	}
	
	protected void unsetProbeManager(IProbeManagerService manager) {
		
	}
	
	protected void setDispatcher(IMessageDispatcherService dispatcherService) {
		registerHandlers(dispatcherService.getServerDispatcher());
	}
	
	protected void unsetDispatcher(IMessageDispatcherService dispatcherService) {
		
	}
	
	protected void setLogManager(ILogManager logManager) {
		logger = logManager.getLogger("Sniffing Daemon");
	}
	
	protected void unsetLogManager(ILogManager logManager) {
		
	}
	
	/*
	 * Probe message handling
	 */
	
	private void requestInterfaceInformation(IMessenger messenger, RequestInterfaceInformation msg) throws MessengerException {
		final List<InterfaceRecord> interfaces = new ArrayList<InterfaceRecord>();
		for(ICaptureInterface iface : getInterfaces()) {
			interfaces.add(new InterfaceRecord(iface.getName(), iface.toString(), iface.captureAvailable(), isEnabled(iface)));
		}
		messenger.emitMessage(msg.createResponse(interfaces));
	}
	
	private void requestModuleInformation(IMessenger messenger, RequestModuleInformation msg) throws MessengerException {
		final List <ModuleRecord> modules = new ArrayList<ModuleRecord>();
		for(ISniffingModule mod : getModules()) {
			modules.add(new ModuleRecord(mod.getName(), isEnabled(mod)));
		}
		messenger.emitMessage(msg.createResponse(modules));
	}
	
	private void setInterfaceEnableState(IMessenger messenger, SetInterfaceEnableState msg) throws MessengerException {
		for(InterfaceRecord iface : msg.getInterfaceRecords()) {
			ICaptureInterface captureInterface = lookupInterfaceByName(iface.getName());
			if (captureInterface == null) {
				logger.warning("No capture interface found with name " + iface.getName());
			} else {
				setEnabled(captureInterface, iface.isEnabled());
			}
		}
		messenger.respondOk(msg);
	}
	
	private ICaptureInterface lookupInterfaceByName(String name) {
		for(ICaptureInterface iface : getInterfaces()) {
			if(iface.getName().equals(name))
				return iface;
		}
		return null;
	}
	private void setModuleEnableState(IMessenger messenger, SetModuleEnableState msg) throws MessengerException {
		for(ModuleRecord module : msg.getModuleRecords()) {
			ISniffingModule sniffingModule = moduleByName.get(module.getName());
			if (sniffingModule == null) {
				logger.warning("No sniffing module found with name " + module.getName());
			} else {
				setEnabled(sniffingModule, module.isEnabled());
			}
		}
		messenger.respondOk(msg);
	}
	
	private void startSniffingDaemon(IMessenger messenger, StartSniffingDaemon msg) throws MessengerException {
		setOpenMessenger(messenger);
		start(msg.getSpaceId());
		messenger.respondOk(msg);
	}
	
	private void stopSniffingDaemon(IMessenger messenger, StopSniffingDaemon msg) throws MessengerException {
		stop();
		messenger.respondOk(msg);
	}
	
	private void captureFileValid(IMessenger messenger, CaptureFileValid msg) throws MessengerException {
		ICaptureFileInterface iface = createCaptureFileInterface(msg.getPath());
		messenger.emitMessage(msg.createResponse(iface.isValid(), iface.getErrorMessage()));
		iface.dispose();
	}
	
	private void sniffingDaemonStatus(IMessenger messenger, SniffingDaemonStatus msg) throws MessengerException {
		messenger.emitMessage(msg.createResponse(isRunning()));
	}
	
	private void runCaptureFile(IMessenger messenger, RunCaptureFile msg) throws MessengerException {
		ICaptureFileInterface iface = createCaptureFileInterface(msg.getPath());
		if(!iface.isValid()) {
			messenger.respondError(msg, iface.getErrorMessage());
			return;
		} else {
			messenger.respondOk(msg);
		}
		setOpenMessenger(messenger);
		CaptureFileRunnable runnable = new CaptureFileRunnable(messenger, iface, this, msg.getSpaceId());
		captureFileThread = new Thread(runnable);
		captureFileThread.start();
	}
	
	private void cancelCaptureFile(IMessenger messenger, CancelCaptureFile msg) throws MessengerException {
		cancelCaptureFile();
		messenger.respondOk(msg);
	}
	
	private void registerHandlers(IMessageDispatcher dispatcher) {
		IMessageHandler handler = new IMessageHandler() {

			public void call(IMessenger messenger, IProbeMessage message)
					throws DispatchException {
				try {
					dispatch(messenger, message);
				} catch (MessengerException e) {
					logger.warning("Error sending message response " + e.getMessage());
				}
			}
		};
		
		dispatcher.registerMessageHandler(RequestInterfaceInformation.ID, handler);
		dispatcher.registerMessageHandler(RequestModuleInformation.ID, handler);
		dispatcher.registerMessageHandler(SetInterfaceEnableState.ID, handler);
		dispatcher.registerMessageHandler(SetModuleEnableState.ID, handler);
		dispatcher.registerMessageHandler(StartSniffingDaemon.ID, handler);
		dispatcher.registerMessageHandler(StopSniffingDaemon.ID, handler);
		dispatcher.registerMessageHandler(CaptureFileValid.ID, handler);
		dispatcher.registerMessageHandler(SniffingDaemonStatus.ID, handler);
		dispatcher.registerMessageHandler(RunCaptureFile.ID, handler);
		dispatcher.registerMessageHandler(CancelCaptureFile.ID, handler);
	}
	
	private void dispatch(IMessenger messenger, IProbeMessage message) throws DispatchMismatchException, MessengerException {
		if(message instanceof RequestInterfaceInformation) {
			requestInterfaceInformation(messenger, (RequestInterfaceInformation) message);
		} else if(message instanceof RequestModuleInformation) {
			requestModuleInformation(messenger, (RequestModuleInformation) message);
		} else if(message instanceof SetInterfaceEnableState) {
			setInterfaceEnableState(messenger, (SetInterfaceEnableState) message);
		} else if(message instanceof SetModuleEnableState) {
			setModuleEnableState(messenger, (SetModuleEnableState) message);
		} else if(message instanceof StartSniffingDaemon) {
			startSniffingDaemon(messenger, (StartSniffingDaemon) message);
		} else if(message instanceof StopSniffingDaemon) {
			stopSniffingDaemon(messenger, (StopSniffingDaemon) message);
		} else if(message instanceof CaptureFileValid) {
			captureFileValid(messenger, (CaptureFileValid) message);
		} else if(message instanceof SniffingDaemonStatus) {
			sniffingDaemonStatus(messenger, (SniffingDaemonStatus) message);
		} else if(message instanceof RunCaptureFile) {
			runCaptureFile(messenger, (RunCaptureFile) message);
		} else if(message instanceof CancelCaptureFile) {
			cancelCaptureFile(messenger, (CancelCaptureFile) message);
		}else {
			throw new DispatchMismatchException(message);
		}
	}
	
	
	
}