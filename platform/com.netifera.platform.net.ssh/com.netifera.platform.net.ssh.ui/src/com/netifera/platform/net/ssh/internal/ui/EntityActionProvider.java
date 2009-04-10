package com.netifera.platform.net.ssh.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;

import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.tools.IToolConfiguration;
import com.netifera.platform.host.filesystem.IFileSystem;
import com.netifera.platform.host.filesystem.ui.OpenFileSystemViewAction;
import com.netifera.platform.host.terminal.ui.OpenTerminalAction;
import com.netifera.platform.net.model.ServiceEntity;
import com.netifera.platform.net.services.credentials.UsernameAndPassword;
import com.netifera.platform.net.services.ssh.SSH;
import com.netifera.platform.net.ssh.filesystem.SFTPFileSystem;
import com.netifera.platform.tools.options.GenericOption;
import com.netifera.platform.tools.options.IntegerOption;
import com.netifera.platform.tools.options.StringOption;
import com.netifera.platform.ui.actions.SpaceAction;
import com.netifera.platform.ui.api.actions.IEntityActionProvider;

public class EntityActionProvider implements IEntityActionProvider {
	
	public List<IAction> getActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();
		
		SSH ssh = (SSH) entity.getAdapter(SSH.class);
		if (ssh != null) {
//			ToolAction sshAuthBruteforcer = new ToolAction("Bruteforce authentication", FTPAuthBruteforcer.class.getName());
//			ftpAuthBruteforcer.setSummary("Try credentials on FTP service.");
//			ftpAuthBruteforcer.addFixedOption(new GenericOption(TCPSocketLocator.class, "target", "Target", "Target FTP service", ftp.getLocator()));
//			ftpAuthBruteforcer.addOption(new IterableOption(UsernameAndPassword.class, "credentials", "Credentials", "List of credentials to try", null));
//			answer.add(ftpAuthBruteforcer);
		}
		
		return answer;
	}

	public List<IAction> getQuickActions(IShadowEntity entity) {
		List<IAction> answer = new ArrayList<IAction>();

		if (entity instanceof ServiceEntity) {
			SSH ssh = (SSH) entity.getAdapter(SSH.class);
			if (ssh != null) {
				SpaceAction action = new OpenFileSystemViewAction("Browse File System") {
					@Override
					public IFileSystem createFileSystem() {
						IToolConfiguration config = getConfiguration();
						return new SFTPFileSystem((SSH)config.get("ssh"), new UsernameAndPassword((String)config.get("username"),(String)config.get("password")));
					}
				};
				action.addFixedOption(new GenericOption(SSH.class, "ssh", "SSH", "SSH server to connect to", ssh));
				action.addOption(new StringOption("username", "Username", "", "root"));
				action.addOption(new StringOption("password", "Password", "", ""));
				answer.add(action);
				
				action = new OpenTerminalAction("Open SSH Terminal", ((ServiceEntity)entity).getAddress().getHost());
				action.addFixedOption(new StringOption("connector", "Connector", "", "com.netifera.platform.net.ssh.terminal.SSHConnector"));
				action.addOption(new StringOption("host", "Host", "Host to connect to", ssh.getLocator().getAddress().toString()));
				action.addOption(new IntegerOption("port", "Port", "Port to connect to", ssh.getLocator().getPort(), 0xFFFF));
				action.addOption(new StringOption("username", "Username", "", "root"));
				action.addOption(new StringOption("password", "Password", "", "", true));
				action.addOption(new StringOption("key", "Public Key", "Public Key to use in the authentication", "", true));
				answer.add(action);
			}
		}
		return answer;
	}
}
