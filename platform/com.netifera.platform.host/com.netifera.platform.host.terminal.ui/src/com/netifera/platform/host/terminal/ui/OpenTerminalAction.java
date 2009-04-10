package com.netifera.platform.host.terminal.ui;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalConnectorExtension;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.UIPlugin;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.host.internal.terminal.ui.Activator;
import com.netifera.platform.host.internal.terminal.ui.ReadOnlySettingsStore;
import com.netifera.platform.host.terminal.ui.view.TerminalView;
import com.netifera.platform.ui.actions.SpaceAction;

public class OpenTerminalAction extends SpaceAction {

	private IEntity hostEntity;
	
	public OpenTerminalAction(String name, IEntity hostEntity) {
		super(name);
		setImageDescriptor(Activator.getInstance().getImageCache().getDescriptor("icons/terminal_view.png"));
		this.hostEntity = hostEntity;
	}
	
	public void setHostEntity(IEntity hostEntity) {
		this.hostEntity = hostEntity;
	}

	@Override
	public void run() {
		IToolConfiguration config = getConfiguration();

		ITerminalConnector conn = TerminalConnectorExtension
				.makeTerminalConnector((String)config.get("connector"));
		if (conn == null) {
			return;
		}
		
		// force instantiating the real connector
		conn.makeSettingsPage();
		long spaceId = getSpace().getId();
		long realmId;
		if (hostEntity != null)
			realmId = hostEntity.getRealmId();
		else
			realmId = getSpace().getProbeId();

		ISettingsStore settings = new ReadOnlySettingsStore(new String[] {
				"Host", (String)config.get("host"),
				"NetworkPort", config.get("port").toString(),
				"User", (String)config.get("username"),
				"Password", (String)config.get("password"),
				"Key", (String)config.get("key"),
				"Space", ""+spaceId,
				"Realm", ""+realmId,
			});
		
		conn.load(settings);
		
/*		ITerminalViewControl ctlTerminal = TerminalViewControlFactory
				.makeControl(this, wndParent, conn);

		ctlTerminal.setConnector(conn);

		ctlTerminal.connectTerminal();
*/
		try {
			IViewPart newTerminalView = UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					"org.eclipse.tm.terminal.view.TerminalView",//$NON-NLS-1$
					"SecondaryTerminal" + System.currentTimeMillis(), //$NON-NLS-1$
					IWorkbenchPage.VIEW_ACTIVATE);
			TerminalView terminal = (TerminalView)newTerminalView;
			
			Boolean commandInputField = (Boolean)config.get("commandInputField");
			if (commandInputField != null) terminal.setCommandInputField(commandInputField);

			terminal.setConnector(conn);
			terminal.setSpace(getSpace());
			terminal.setHostEntity(hostEntity);
			terminal.onTerminalConnect();
//			conn.connect(terminal);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
