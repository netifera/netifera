package com.netifera.platform.ui.internal.spaces;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.model.IModelService;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.probe.IProbeManagerService;
import com.netifera.platform.ui.api.actions.IEntityActionProviderService;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProviderService;
import com.netifera.platform.ui.api.model.IEntityLabelProviderService;
import com.netifera.platform.ui.application.ApplicationPlugin;
import com.netifera.platform.ui.application.Perspective;
import com.netifera.platform.ui.images.ImageCache;
import com.netifera.platform.ui.spaces.IStatusContribution;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.spaces.actions.SpaceCreator;
import com.netifera.platform.ui.spaces.inputbar.InputBar;
import com.netifera.platform.ui.spaces.inputbar.InputBarAction;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualizationFactory;
import com.netifera.platform.ui.workbench.IWorkbenchChangeListener;
import com.netifera.platform.ui.workbench.WorkbenchChangeManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.netifera.platform.ui.spaces";

	// The shared instance
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	
	private ServiceTracker modelTracker;
	private ServiceTracker probeManagerTracker;
	private ServiceTracker modelLabelsTracker;
	private ServiceTracker actionProviderServiceTracker;
	private ServiceTracker inputBarActionProviderTracker;
	private ServiceTracker logManagerTracker;
	private ServiceTracker visualizationFactoryTracker;
	
	private ServiceTracker statusContributionTracker;

	private InputBar inputBar;
	private IContributionItem inputBarItem;
	private IWorkbenchAction inputBarAction;
	
	private ToolBarContributionItem toolbarItem;
	private boolean isDisabled;
	private boolean saveActionState;


	private WorkbenchChangeManager workbenchManager;
	private ImageCache imageCache;

	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void initialize() {
		workbenchManager = new WorkbenchChangeManager(getWindow(), Perspective.ID, createChangeListener());
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				workbenchManager.initialize();					
			}
		});
	}
	
	private void createFirstSpaceIfNeeded(IWorkbenchWindow window) {
		if(hasSpaces()) return;
		SpaceCreator creator = new SpaceCreator(window);
		creator.create();
	}
	
	private boolean hasSpaces() {
		return getModel().getCurrentWorkspace().getAllSpaces().size() > 0;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		imageCache = new ImageCache(PLUGIN_ID);
		
		modelTracker = new ServiceTracker(context, IModelService.class.getName(), null);
		modelTracker.open();
		
		probeManagerTracker = new ServiceTracker(context, IProbeManagerService.class.getName(), null);
		probeManagerTracker.open();
		
		modelLabelsTracker = new ServiceTracker(context, IEntityLabelProviderService.class.getName(), null);
		modelLabelsTracker.open();
		
		actionProviderServiceTracker = new ServiceTracker(context, IEntityActionProviderService.class.getName(), null);
		actionProviderServiceTracker.open();
		
		inputBarActionProviderTracker = new ServiceTracker(context, IInputBarActionProviderService.class.getName(), null);
		inputBarActionProviderTracker.open();
		
		logManagerTracker = new ServiceTracker(context, ILogManager.class.getName(), null);
		logManagerTracker.open();

		visualizationFactoryTracker = new ServiceTracker(context, ISpaceVisualizationFactory.class.getName(), null);
		visualizationFactoryTracker.open();
		
		statusContributionTracker = new ServiceTracker(context, IStatusContribution.class.getName(), null);
		statusContributionTracker.open();
	}

	private IWorkbenchChangeListener createChangeListener() {
		return new IWorkbenchChangeListener() {

			public void activePageOpened(IWorkbenchPage page) {
				runActivePageSetup(page);					
			}

			public void partChange() {
				updateState();		
//				setStatusLine();
			}

			public void perspectiveClosed() {
				if(toolbarItem != null) {
					ApplicationPlugin.getDefault().getCoolBar().remove(toolbarItem);
					toolbarItem.dispose();
					toolbarItem = null;
					inputBarItem = null;
					inputBarAction = null;
				}				
			}

			public void perspectiveOpened() {
				displayToolbar();				
			}
			
		};
	}
	
	private void runActivePageSetup(final IWorkbenchPage page) {
		closeDeadEditors(page);
		getWorkbench().getDisplay().asyncExec(new Runnable() {

			public void run() {
				createFirstSpaceIfNeeded(page.getWorkbenchWindow());
//				setStatusLine();
			}
				
		});
	}
	
	private void closeDeadEditors(final IWorkbenchPage page) {
		/* Detect RCP error editors */
		final IEditorReference[] editorReferences = page.getEditorReferences();
		for(IEditorReference ref : editorReferences) {
			IEditorPart part = ref.getEditor(true);
			IEditorInput editorInput = part.getEditorInput();
			if(!(editorInput instanceof SpaceEditorInput)) {
				page.closeEditor(part, false);
			}
	
			
		}
	}
	
	private void updateState() {
		
		enableToolbar();
		IEditorPart editor = getActiveEditor();
		if(editor == null) {
			disableToolbar("Open a space to enable.");
			return;
		}
		IEditorInput input = editor.getEditorInput();
		if(!(input instanceof SpaceEditorInput)) {
			disableToolbar("Internal error");
			return;
		}
		IProbe probe = ((SpaceEditorInput) input).getProbeForSpace();
		if(!probe.isConnected()) {
			disableToolbar("Connect to probe to enable.");
		}
			
	}
	
	private void disableToolbar(String message) {
		if(isDisabled || toolbarItem == null) return;
		isDisabled = true;
		saveActionState = inputBarAction.isEnabled();
		inputBarAction.setEnabled(false);
		inputBar.setDisabled(message);
	}
	
	private void enableToolbar() {
		if(!isDisabled || toolbarItem == null) return;
		isDisabled = false;
		inputBarAction.setEnabled(saveActionState);
		inputBar.setEnabled();
	}
	private IWorkbenchWindow getWindow() {
		IWorkbenchWindow[] windows = getWorkbench().getWorkbenchWindows();
		if(windows.length == 0) {
			return null;
		}
		return windows[0];
	}
	
	private void displayToolbar() {	
		if(toolbarItem != null) {
			return;
		}
		final ICoolBarManager coolbar = ApplicationPlugin.getDefault().getCoolBar();
		final IToolBarManager toolbar = new ToolBarManager(coolbar.getStyle());
		toolbarItem = new ToolBarContributionItem(toolbar);
		coolbar.add(toolbarItem);
//		coolbar.appendToGroup("space-actions", toolbarItem);

		inputBar = new InputBar("input.bar");
		inputBarItem = inputBar;
		inputBarAction = new InputBarAction(inputBar);
		
		toolbar.add(inputBarItem);
		toolbar.add(inputBarAction);
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				coolbar.update(true);	
				updateState();
			}
		});
	}
	
/*	private void setStatusLine() {
		IEditorPart editor = getActiveEditor();
		if(editor == null) 
			return;
		
		IStatusLineManager statusLine = editor.getEditorSite().getActionBars().getStatusLineManager();
		// In case it already exists
		statusLine.remove(statusLineItem);
		statusLine.add(statusLineItem);
		statusLine.update(true);
	}
*/	
	
	public void stop(BundleContext context) throws Exception {
		imageCache.dispose();
		imageCache = null;
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	
	public IModelService getModel() {
		return (IModelService) modelTracker.getService();
	}
	
	public IProbeManagerService getProbeManager() {
		return (IProbeManagerService) probeManagerTracker.getService();
	}
	
	public IEntityLabelProviderService getLabelProvider() {
		return (IEntityLabelProviderService) modelLabelsTracker.getService();
	}
	
	public IEntityActionProviderService getActionProvider() {
		return (IEntityActionProviderService) actionProviderServiceTracker.getService();
	}
	
	public IInputBarActionProviderService getInputBarActionProvider() {
		return (IInputBarActionProviderService) inputBarActionProviderTracker.getService();
	}
	
	public ILogManager getLogManager() {
		return (ILogManager) logManagerTracker.getService();
	}
	
	public ImageCache getImageCache() {
		return imageCache;
	}

	public ISpaceVisualizationFactory getVisualizationFactory() {
		try {
			return (ISpaceVisualizationFactory) visualizationFactoryTracker.waitForService(10000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	public IStatusContribution getStatusContribution() {
		return (IStatusContribution) statusContributionTracker.getService();
	}
	public IEditorPart getActiveEditor() {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		if(window == null) {
			return null;
		}
		IWorkbenchPage activePage = window.getActivePage();
		if(activePage == null) {
			return null;
		}
		return activePage.getActiveEditor();
	}
}
