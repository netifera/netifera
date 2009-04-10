/*******************************************************************************
 * Copyright (c) 2003, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Martin Oberhuber (Wind River) - [206892] State handling: Only allow connect when CLOSED
 * Michael Scharf (Wind River) - [209656] ClassCastException in TerminalView under Eclipse-3.4M3
 * Michael Scharf (Wind River) - [189774] Ctrl+V does not work in the command input field.
 * Michael Scharf (Wind River) - [217999] Duplicate context menu entries in Terminal
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 * Martin Oberhuber (Wind River) - [168186] Add Terminal User Docs
 * Michael Scharf (Wind River) - [172483] switch between connections
 * Michael Scharf (Wind River) - [240023] Get rid of the terminal's "Pin" button
 * Michael Scharf (Wind River) - [196454] Initial connection settings dialog should not be blank
 *******************************************************************************/
package com.netifera.platform.host.terminal.ui.view;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tm.internal.terminal.control.ITerminalListener;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.control.TerminalViewControlFactory;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionClearAll;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCopy;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionCut;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionPaste;
import org.eclipse.tm.internal.terminal.control.actions.TerminalActionSelectAll;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.LayeredSettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.PreferenceSettingStore;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.terminal.ui.actions.ConfigureAction;
import com.netifera.platform.host.terminal.ui.actions.ConnectAction;
import com.netifera.platform.host.terminal.ui.actions.DisconnectAction;
import com.netifera.platform.host.terminal.ui.actions.NewTerminalAction;
import com.netifera.platform.host.terminal.ui.actions.RemoveTerminalAction;
import com.netifera.platform.host.terminal.ui.actions.ScrollLockAction;
import com.netifera.platform.host.terminal.ui.actions.SelectTerminalAction;
import com.netifera.platform.host.terminal.ui.actions.TerminalViewAction;
import com.netifera.platform.host.terminal.ui.actions.ToggleCommandInputFieldAction;
import com.netifera.platform.host.terminal.ui.view.ITerminalViewConnectionManager.ITerminalViewConnectionFactory;
import com.netifera.platform.host.terminal.ui.view.ITerminalViewConnectionManager.ITerminalViewConnectionListener;
import com.netifera.platform.host.terminal.ui.view.internal.PageBook;
import com.netifera.platform.host.terminal.ui.view.internal.SettingStorePrefixDecorator;
import com.netifera.platform.host.terminal.ui.view.internal.SettingsStore;
import com.netifera.platform.host.terminal.ui.view.internal.TerminalPreferencePage;
import com.netifera.platform.host.terminal.ui.view.internal.TerminalSettingsDialog;

public class TerminalView extends ViewPart implements ITerminalView, ITerminalViewConnectionListener {
    private static final String PREF_CONNECTORS = "Connectors."; //$NON-NLS-1$
	private static final String STORE_CONNECTION_TYPE = "ConnectionType"; //$NON-NLS-1$
    private static final String STORE_SETTING_SUMMARY = "SettingSummary"; //$NON-NLS-1$
	private static final String STORE_TITLE = "Title"; //$NON-NLS-1$
	public static final String  FONT_DEFINITION = "terminal.views.view.font.definition"; //$NON-NLS-1$

	private ISpace space;
	private IEntity hostEntity;
	
	protected ITerminalViewControl terminalViewControl;

	// TODO (scharf): this decorator is only there to deal wit the common
	// actions. Find a better solution.
	TerminalViewControlDecorator fCtlDecorator=new TerminalViewControlDecorator();
	
	protected TerminalViewAction newTerminalAction;
	protected TerminalViewAction connectAction;
	protected TerminalViewAction scrollLockAction;
	protected TerminalViewAction disconnectAction;
	protected TerminalViewAction configureAction;
	protected TerminalActionCopy fActionEditCopy;
	protected TerminalActionCut fActionEditCut;
	protected TerminalActionPaste fActionEditPaste;
	protected TerminalActionClearAll fActionEditClearAll;
	protected TerminalActionSelectAll fActionEditSelectAll;
	protected TerminalViewAction toggleInputFieldAction;
	protected TerminalPropertyChangeHandler fPropertyChangeHandler;

	protected Action selectTerminalAction;
	protected Action removeTerminalAction;
	
//	protected TerminalViewAction runAction;

	protected boolean fMenuAboutToShow;

	private SettingsStore fStore;

	private final ITerminalViewConnectionManager fMultiConnectionManager=new TerminalViewConnectionManager();
	
	/**
	 * Listens to changes in the preferences
	 */
	private final IPropertyChangeListener fPreferenceListener=new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(event.getProperty().equals(TerminalPreferencePage.PREF_LIMITOUTPUT)
					|| event.getProperty().equals(TerminalPreferencePage.PREF_BUFFERLINES)
					|| event.getProperty().equals(TerminalPreferencePage.PREF_INVERT_COLORS)) {
				updatePreferences();
			}
		}
	};

	private PageBook fPageBook;

	/**
	 * This listener updates both, the view and the 
	 * ITerminalViewConnection.
	 *
	 */
	class TerminalListener implements ITerminalListener {
		volatile ITerminalViewConnection fConnection;
		void setConnection(ITerminalViewConnection connection) {
			fConnection=connection;
		}
		public void setState(final TerminalState state) {
			runInDisplayThread(new Runnable() {
				public void run() {
					fConnection.setState(state);
					// if the active connection changes, update the view
					if(fConnection==fMultiConnectionManager.getActiveConnection()) {
						updateStatus();
					}
				}
			});
		}
		public void setTerminalTitle(final String title) {
			runInDisplayThread(new Runnable() {
				public void run() {
					fConnection.setTerminalTitle(title);
					// if the active connection changes, update the view
					if(fConnection==fMultiConnectionManager.getActiveConnection()) {
						updateSummary();
					}
				}
			});
		}
		/**
		 * @param runnable run in display thread
		 */
		private void runInDisplayThread(Runnable runnable) {
			if(Display.findDisplay(Thread.currentThread())!=null)
				runnable.run();
			else if(PlatformUI.isWorkbenchRunning())
				PlatformUI.getWorkbench().getDisplay().syncExec(runnable);
			// else should not happen and we ignore it...
		}
		
	}
	
	public TerminalView() {
		Logger
				.log("==============================================================="); //$NON-NLS-1$
		fMultiConnectionManager.addListener(this);
	}

	/**
	 * @param title
	 * @return a unique part name
	 */
	String findUniqueTitle(String title) {
		IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
		String id=	getViewSite().getId();
		Set names=new HashSet();
		for (int i = 0; i < pages.length; i++) {
			IViewReference[] views = pages[i].getViewReferences();
			for (int j = 0; j < views.length; j++) {
				IViewReference view = views[j];
				// only look for views with the same ID
				if(id.equals(view.getId())) {
					String name=view.getTitle();
					if(name!=null)
						names.add(view.getPartName());
				}
			}
		}
		// find a unique name
		int i=1;
		String uniqueTitle=title;
		while(true) {
			if(!names.contains(uniqueTitle))
				return uniqueTitle;
			uniqueTitle=title+" "+i++; //$NON-NLS-1$
		}
	}
	/**
	 * Update the text limits from the preferences
	 */
	private void updatePreferences() {
		Preferences preferences = Activator.getInstance().getPluginPreferences();
//		boolean limitOutput = preferences.getBoolean(TerminalPreferencePage.PREF_LIMITOUTPUT);
//		if(!limitOutput)
//			bufferLineLimit=-1;
		int bufferLineLimit = preferences.getInt(TerminalPreferencePage.PREF_BUFFERLINES);
		boolean invert=preferences.getBoolean(TerminalPreferencePage.PREF_INVERT_COLORS);
		// update the preferences for all controls
		ITerminalViewConnection[] conn=fMultiConnectionManager.getConnections();
		for (int i = 0; i < conn.length; i++) {
			conn[i].getCtlTerminal().setBufferLineLimit(bufferLineLimit);
			conn[i].getCtlTerminal().setInvertedColors(invert);
		}
	}
	/**
	 * Display a new Terminal view.  This method is called when the user clicks the New
	 * Terminal button in any Terminal view's toolbar.
	 */
	public void onTerminalNewTerminal() {
		Logger.log("creating new Terminal instance."); //$NON-NLS-1$
		setupControls();
		if(newConnection("New Terminal")==null) {
			fMultiConnectionManager.removeActive();
		}
	}

	public void onTerminalNewView() {
		try {
			// The second argument to showView() is a unique String identifying the
			// secondary view instance.  If it ever matches a previously used secondary
			// view identifier, then this call will not create a new Terminal view,
			// which is undesirable.  Therefore, we append the active time in
			// milliseconds to the secondary view identifier to ensure it is always
			// unique.  This code runs only when the user clicks the New Terminal
			// button, so there is no risk that this code will run twice in a single
			// millisecond.
			IViewPart newTerminalView = getSite().getPage().showView(
					"org.eclipse.tm.terminal.view.TerminalView",//$NON-NLS-1$
					"SecondaryTerminal" + System.currentTimeMillis(), //$NON-NLS-1$
					IWorkbenchPage.VIEW_ACTIVATE);
			if(newTerminalView instanceof ITerminalView) {
				ITerminalConnector c = ((TerminalView)newTerminalView).newConnection("New Terminal View");
				// if there is no connector selected, hide the new view
				if(c==null) {
					getSite().getPage().hideView(newTerminalView);
				}
			}
		} catch (PartInitException ex) {
			Logger.logException(ex);
		}
	}

	public void onTerminalConnect() {
		//if (isConnected())
		if (terminalViewControl.getState()!=TerminalState.CLOSED)
			return;
		if(terminalViewControl.getTerminalConnector()==null)
			setConnector(showSettingsDialog("Settings"));
		terminalViewControl.connectTerminal();
	}

	private void updateStatus() {
		updateTerminalConnect();
		updateTerminalDisconnect();
		updateTerminalSettings();
		toggleInputFieldAction.setChecked(hasCommandInputField());
		updateSummary();
		updateTitle();
	}
	
	private void updateTitle() {
		String icon = "terminal_disconnected.png";
		if (isConnecting())
			icon = "terminal_connecting.png";
		else if (terminalViewControl.isConnected())
			icon = "terminal_connected.png";
		setTitleImage(Activator.getInstance().getImageCache().get("icons/"+icon));
	}

	private void updateTerminalConnect() {
		//boolean bEnabled = ((!isConnecting()) && (!fCtlTerminal.isConnected()));
		boolean bEnabled = (terminalViewControl.getState()==TerminalState.CLOSED);

		connectAction.setEnabled(bEnabled);
	}

	public boolean isConnecting() {
		return terminalViewControl.getState()==TerminalState.CONNECTING
		    || terminalViewControl.getState()==TerminalState.OPENED;
	}
	
	public void onTerminalDisconnect() {
		terminalViewControl.disconnectTerminal();
	}

	public void updateTerminalDisconnect() {
		boolean bEnabled = ((isConnecting()) || (terminalViewControl.isConnected()));
		disconnectAction.setEnabled(bEnabled);
	}

	public void onTerminalSettings() {
		showSettingsDialog(null);
	}

	public void onTerminalRunAction() {
		//TODO
	}

	private ITerminalConnector newConnection(String title) {
		ITerminalConnector c=showSettingsDialog(title);
		if(c!=null) {
			setConnector(c);
			onTerminalConnect();
		}
		return c;
	}

	private ITerminalConnector showSettingsDialog(String title) {
		// When the settings dialog is opened, load the Terminal settings from the
		// persistent settings.

		ITerminalConnector[] connectors = terminalViewControl.getConnectors();
		if(terminalViewControl.getState()!=TerminalState.CLOSED)
			connectors=new ITerminalConnector[0];
		// load the state from the settings
		// first load from fStore and then from the preferences.
		ITerminalConnector c = loadSettings(new LayeredSettingsStore(fStore,getPreferenceSettingsStore()), connectors);
		// if we have no connector show the one from the settings 
		if(terminalViewControl.getTerminalConnector()!=null)
			c=terminalViewControl.getTerminalConnector();
		TerminalSettingsDialog dlgTerminalSettings = new TerminalSettingsDialog(getViewSite().getShell(),connectors,c);
		dlgTerminalSettings.setTerminalTitle(getActiveConnection().getPartName());
		if(title!=null)
			dlgTerminalSettings.setTitle(title);
		Logger.log("opening Settings dialog."); //$NON-NLS-1$

		if (dlgTerminalSettings.open() == Window.CANCEL) {
			Logger.log("Settings dialog cancelled."); //$NON-NLS-1$
			return null;
		}

		Logger.log("Settings dialog OK'ed."); //$NON-NLS-1$

		// When the settings dialog is closed, we persist the Terminal settings.
		saveSettings(fStore,dlgTerminalSettings.getConnector());
		// we also save it in the preferences. This will keep the last change
		// made to this connector as default...
		saveSettings(getPreferenceSettingsStore(), dlgTerminalSettings.getConnector());

		setViewTitle(dlgTerminalSettings.getTerminalTitle());
		return dlgTerminalSettings.getConnector();
	}

	public void setConnector(ITerminalConnector connector) {
		terminalViewControl.setConnector(connector);
	}

	public void updateTerminalSettings() {
//		fActionTerminalSettings.setEnabled((fCtlTerminal.getState()==TerminalState.CLOSED));
	}
	private void setViewTitle(String title) {
		setPartName(title);
		getActiveConnection().setPartName(title);
	}
	private void setViewSummary(String summary) {
		setContentDescription(summary);
		getViewSite().getActionBars().getStatusLineManager().setMessage(
				summary);
		setTitleToolTip(getPartName()+": "+summary); //$NON-NLS-1$
		
	}
	public void updateSummary() {
		setViewSummary(getActiveConnection().getFullSummary());
	}

	public void onTerminalFontChanged() {
		// set the font for all 
		Font font=JFaceResources.getFont(FONT_DEFINITION);
		ITerminalViewConnection[] conn=fMultiConnectionManager.getConnections();
		for (int i = 0; i < conn.length; i++) {
			conn[i].getCtlTerminal().setFont(font);
		}
	}

	// ViewPart interface

	public void createPartControl(Composite wndParent) {
		// Bind plugin.xml key bindings to this plugin.  Overrides global Control-W key
		// sequence.

		fPageBook=new PageBook(wndParent,SWT.NONE);
		ISettingsStore s=new SettingStorePrefixDecorator(fStore,"connectionManager"); //$NON-NLS-1$
		fMultiConnectionManager.loadState(s,new ITerminalViewConnectionFactory() {
			public ITerminalViewConnection create() {
				return makeViewConnection();
			}
		});
		// if there is no connection loaded, create at least one
		// needed to read old states from the old terminal
		if(fMultiConnectionManager.size()==0) {
			ITerminalViewConnection conn = makeViewConnection();
			fMultiConnectionManager.addConnection(conn);
			fMultiConnectionManager.setActiveConnection(conn);
			fPageBook.showPage(terminalViewControl.getRootControl());
		}
		setTerminalControl(fMultiConnectionManager.getActiveConnection().getCtlTerminal());
		setViewTitle(findUniqueTitle("Terminal"));
		setupActions();
		setupLocalToolBars();
		// setup all context menus
		ITerminalViewConnection[] conn=fMultiConnectionManager.getConnections();
		for (int i = 0; i < conn.length; i++) {
			setupContextMenus(conn[i].getCtlTerminal().getControl());
		}
		setupListeners(wndParent);

//		PlatformUI.getWorkbench().getHelpSystem().setHelp(wndParent, Activator.HELPPREFIX + "terminal_page"); //$NON-NLS-1$

		legacyLoadState();
		legacySetTitle();

		refresh();
		onTerminalFontChanged();

	}

	public void dispose() {
		Logger.log("entered."); //$NON-NLS-1$

		Activator.getInstance().getPreferenceStore().removePropertyChangeListener(fPreferenceListener);

		JFaceResources.getFontRegistry().removeListener(fPropertyChangeHandler);
		
		// dispose all connections
		ITerminalViewConnection[] conn=fMultiConnectionManager.getConnections();
		for (int i = 0; i < conn.length; i++) {
			conn[i].getCtlTerminal().disposeTerminal();
		}
		super.dispose();
	}
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		terminalViewControl.setFocus();
	}

	/**
	 * This method creates the top-level control for the Terminal view.
	 */
	protected void setupControls() {
		ITerminalViewConnection conn = makeViewConnection();
		fMultiConnectionManager.addConnection(conn);
		fMultiConnectionManager.setActiveConnection(conn);
		setupContextMenus(terminalViewControl.getControl());
	}

	private ITerminalViewConnection makeViewConnection() {
		ITerminalConnector[] connectors = makeConnectors();
		TerminalListener listener=new TerminalListener();
		ITerminalViewControl ctrl = TerminalViewControlFactory.makeControl(listener, fPageBook, connectors);
		setTerminalControl(ctrl);
		ITerminalViewConnection conn = new TerminalViewConnection(terminalViewControl);
		listener.setConnection(conn);
		conn.setPartName(getPartName());
		// load from settings
		ITerminalConnector connector = loadSettings(fStore,connectors);
		// set the connector....
		ctrl.setConnector(connector);
		updatePreferences();
		Activator.getInstance().getPreferenceStore().addPropertyChangeListener(fPreferenceListener);
		
		return conn;
	}

	/**
	 * @param store contains the data
	 * @param connectors loads the data from store
	 * @return null or the currently selected connector
	 */
	private ITerminalConnector loadSettings(ISettingsStore store, ITerminalConnector[] connectors) {
		System.err.println("TerminalView.loadSettings");
		
		ITerminalConnector connector=null;
		String connectionType=store.get(STORE_CONNECTION_TYPE);
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].load(getStore(store,connectors[i]));
			if(connectors[i].getId().equals(connectionType))
				connector=connectors[i];
		}
		return connector;
	}

	/**
	 * @return a list of connectors this view can use
	 */
	protected ITerminalConnector[] makeConnectors() {
		ITerminalConnector[] connectors=TerminalConnectorExtension.makeTerminalConnectors();
		return connectors;
	}

	/**
	 * The preference setting store is used to save the settings that are
	 * shared between all views. 
	 * @return the settings store for the connection based on the preferences.
	 * 
	 */
	private PreferenceSettingStore getPreferenceSettingsStore() {
		return new PreferenceSettingStore(Activator.getInstance().getPluginPreferences(),PREF_CONNECTORS);
	}
	
	/**
	 * @param store the settings will be saved in this store
	 * @param connector the connector that will be saved. Can be null.
	 */
	private void saveSettings(ISettingsStore store, ITerminalConnector connector) {
		if(connector!=null) {
			connector.save(getStore(store, connector));
			// the last saved connector becomes the default
			store.put(STORE_CONNECTION_TYPE,connector.getId());
		}
		
	}
	
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fStore=new SettingsStore(memento);
	}
	
	public void saveState(IMemento memento) {
		super.saveState(memento);
		fStore.put(STORE_TITLE,getPartName());
		fMultiConnectionManager.saveState(new SettingStorePrefixDecorator(fStore,"connectionManager")); //$NON-NLS-1$
		fStore.saveState(memento);
	}
	
	private ISettingsStore getStore(ISettingsStore store, ITerminalConnector connector) {
		return new SettingStorePrefixDecorator(store,connector.getId()+"."); //$NON-NLS-1$
	}

	protected void setupActions() {
		selectTerminalAction = new SelectTerminalAction(fMultiConnectionManager);
		removeTerminalAction=new RemoveTerminalAction(fMultiConnectionManager);
		newTerminalAction = new NewTerminalAction(this);
		scrollLockAction = new ScrollLockAction(this);
		connectAction = new ConnectAction(this);
		disconnectAction = new DisconnectAction(this);
		configureAction = new ConfigureAction(this);
//		runAction = new RunAction(this);
		fActionEditCopy = new TerminalActionCopy(fCtlDecorator);
		fActionEditCut = new TerminalActionCut(fCtlDecorator);
		fActionEditPaste = new TerminalActionPaste(fCtlDecorator);
		fActionEditClearAll = new TerminalActionClearAll(fCtlDecorator);
		fActionEditSelectAll = new TerminalActionSelectAll(fCtlDecorator);
		toggleInputFieldAction = new ToggleCommandInputFieldAction(this);
	}
	
	protected void setupLocalToolBars() {
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();

		toolBarMgr.add(connectAction);
		toolBarMgr.add(disconnectAction);
		toolBarMgr.add(new Separator("fixedGroup"));
//		toolBarMgr.add(runAction);
		toolBarMgr.add(new Separator("fixedGroup"));
		toolBarMgr.add(configureAction);
		toolBarMgr.add(toggleInputFieldAction);
		toolBarMgr.add(scrollLockAction);
		toolBarMgr.add(new Separator("fixedGroup"));
		toolBarMgr.add(selectTerminalAction);
		toolBarMgr.add(newTerminalAction);
		toolBarMgr.add(removeTerminalAction);
	}

	protected void setupContextMenus(Control ctlText) {
		MenuManager menuMgr;
		Menu menu;
		TerminalContextMenuHandler contextMenuHandler;

		menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menu = menuMgr.createContextMenu(ctlText);
		loadContextMenus(menuMgr);
		contextMenuHandler = new TerminalContextMenuHandler();

		ctlText.setMenu(menu);
		menuMgr.addMenuListener(contextMenuHandler);
		menu.addMenuListener(contextMenuHandler);
	}

	protected void loadContextMenus(IMenuManager menuMgr) {
		menuMgr.add(fActionEditCopy);
		menuMgr.add(fActionEditPaste);
		menuMgr.add(new Separator());
		menuMgr.add(fActionEditClearAll);
		menuMgr.add(fActionEditSelectAll);
		menuMgr.add(new Separator());
		menuMgr.add(toggleInputFieldAction);
		menuMgr.add(scrollLockAction);

		// Other plug-ins can contribute there actions here
		menuMgr.add(new Separator("Additions")); //$NON-NLS-1$
	}

	protected void setupListeners(Composite wndParent) {
		fPropertyChangeHandler = new TerminalPropertyChangeHandler();
		JFaceResources.getFontRegistry().addListener(fPropertyChangeHandler);
	}

	protected class TerminalContextMenuHandler implements MenuListener, IMenuListener {
		public void menuHidden(MenuEvent event) {
			fMenuAboutToShow = false;
			fActionEditCopy.updateAction(fMenuAboutToShow);
		}

		public void menuShown(MenuEvent e) {
			//
		}
		public void menuAboutToShow(IMenuManager menuMgr) {
			fMenuAboutToShow = true;
			fActionEditCopy.updateAction(fMenuAboutToShow);
			fActionEditCut.updateAction(fMenuAboutToShow);
			fActionEditSelectAll.updateAction(fMenuAboutToShow);
			fActionEditPaste.updateAction(fMenuAboutToShow);
			fActionEditClearAll.updateAction(fMenuAboutToShow);
		}
	}

	protected class TerminalPropertyChangeHandler implements IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FONT_DEFINITION)) {
				onTerminalFontChanged();
			}
		}
	}

	public boolean hasCommandInputField() {
		return getActiveConnection().hasCommandInputField();
	}

	public void setCommandInputField(boolean on) {
		getActiveConnection().setCommandInputField(on);
	}

	public boolean isScrollLock() {
		return terminalViewControl.isScrollLock();
	}

	public void setScrollLock(boolean on) {
		terminalViewControl.setScrollLock(on);
	}

	private ITerminalViewConnection getActiveConnection() {
		return fMultiConnectionManager.getActiveConnection();
	}
	/**
	 * @param ctrl this control becomes the currently used one
	 */
	private void setTerminalControl(ITerminalViewControl ctrl) {
		terminalViewControl=ctrl;
		fCtlDecorator.setViewContoler(ctrl);
	}
	public void connectionsChanged() {
		if(getActiveConnection()!=null) {
			// update the active {@link ITerminalViewControl}
			ITerminalViewControl ctrl = getActiveConnection().getCtlTerminal();
			if(terminalViewControl!=ctrl) {
				setTerminalControl(ctrl);
				refresh();
			}
		}	
	}

	/**
	 * Show the active {@link ITerminalViewControl} in the view
	 */
	private void refresh() {
		fPageBook.showPage(terminalViewControl.getRootControl());
		updateStatus();
		setPartName(getActiveConnection().getPartName());
	}
	/**
	 * TODO REMOVE This code (added 2008-06-11)
	 * Legacy code to real the old state. Once the state of the
	 * terminal has been saved this method is not needed anymore.
	 * Remove this code with eclipse 3.5.
	 */
	private void legacyLoadState() {
		// TODO legacy: load the old title....
		String summary=fStore.get(STORE_SETTING_SUMMARY);
		if(summary!=null) {
			getActiveConnection().setSummary(summary);
			fStore.put(STORE_SETTING_SUMMARY,null);
		}
	}
	/**
	 * TODO REMOVE This code (added 2008-06-11)
	 * Legacy code to real the old state. Once the state of the
	 * terminal has been saved this method is not needed anymore.
	 * Remove this code with eclipse 3.5.
	 */
	private void legacySetTitle() {
		// restore the title of this view
		String title=fStore.get(STORE_TITLE);
		if(title!=null && title.length()>0) {
			setViewTitle(title);
			fStore.put(STORE_TITLE, null);
		}
	}

	public void setSpace(ISpace space) {
		this.space = space;
	}

	public ISpace getSpace() {
		return space;
	}

	public void setHostEntity(IEntity entity) {
		this.hostEntity = entity;
	}
	
	public IEntity getHostEntity() {
		return hostEntity;
	}
}
