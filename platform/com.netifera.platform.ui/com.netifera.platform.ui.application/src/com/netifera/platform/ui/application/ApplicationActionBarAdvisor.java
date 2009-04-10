package com.netifera.platform.ui.application;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	private IContributionItem viewList;
	private IContributionItem perspectiveList;
	private IWorkbenchAction resetPerspectiveAction;
	private IWorkbenchAction aboutAction;
	private IWorkbenchAction preferenceAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
		resetPerspectiveAction = ActionFactory.RESET_PERSPECTIVE.create(window);
		register(resetPerspectiveAction);
		
		/* the following is to fill the netifera application menu in Mac OSX*/
        aboutAction =  ActionFactory.ABOUT.create(window); 
        aboutAction.setText("&About");
        preferenceAction =  ActionFactory.PREFERENCES.create(window); 
        register(aboutAction);
        register(preferenceAction);
        
	}

	protected void fillCoolBar(ICoolBarManager coolBar) {
		ApplicationPlugin.getDefault().setCoolBar(coolBar);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager winMenu = new MenuManager("&Window",IWorkbenchActionConstants.M_WINDOW);
		winMenu.add(preferenceAction);
		
		MenuManager viewMenu = new MenuManager("Open View");
		viewMenu.add(viewList);
		winMenu.add(viewMenu);
		
		MenuManager perspectiveMenu = new MenuManager("Open Perspective");
		winMenu.add(perspectiveMenu);

		
		MenuManager helpMenu = new MenuManager("&Help",IWorkbenchActionConstants.M_HELP);
		helpMenu.add(aboutAction);

		perspectiveMenu.add(perspectiveList);
		
		menuBar.add(winMenu);
		menuBar.add(helpMenu);
	}
}