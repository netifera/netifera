package com.netifera.platform.ui.spaces.inputbar;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.ui.api.actions.ISpaceAction;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProviderService;
import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.spaces.actions.SpaceCreator;
import com.netifera.platform.ui.util.GreyedText;


public class InputBar extends ControlContribution {
	private final RGB WARNING_COLOR = new RGB(0xF5, 0xA9, 0xA9);
	private String content = "";
	private IAction buttonAction;
	private List<IAction> availableActions = new ArrayList<IAction>();
	private Text text;
	private Color warningColor;
	private ILogger logger;
	
	public InputBar(String id) {
		super(id);
		logger = Activator.getDefault().getLogManager().getLogger("Input Bar");
	}

	public void setAction(IAction action) {
		this.buttonAction = action;
	}
	
	public void setEnabled() {
		if(text == null || text.isDisposed()) return;
		
		text.setEnabled(true);
		text.setText("");
		text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}
	
	public void setDisabled(String message) {
		if(text == null || text.isDisposed()) return;
		text.setEnabled(false);
		text.setText(message);
		text.setBackground(warningColor);
	}

	private String getDefaultToolTipText() {
		return "Enter new entity ('192.168.1.1', '192.168.1.0/24', 'www.netifera.com', '.netifera.com', 'http://netifera.com', ...)";
	}
	
	@Override
	protected Control createControl(Composite parent) {
		text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.setTextLimit(100);
		text.setToolTipText(getDefaultToolTipText());
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				content = text.getText();
				InputBar.this.doUpdate();
				if (availableActions.size()>0)
					text.setToolTipText(availableActions.get(0).getText());
				else
					if (content.length() == 0)
						text.setToolTipText(InputBar.this.getDefaultToolTipText());
					else
						text.setToolTipText(null);
			}
		});
		text.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					if (InputBar.this.actionEnabled()) {
						if ((e.stateMask & SWT.SHIFT) != 0) {
							SpaceCreator creator = new SpaceCreator(Activator.getDefault().getWorkbench().getActiveWorkbenchWindow());
							creator.create(content);
						}
						InputBar.this.runAction();
						text.setText(content);
						InputBar.this.update();
					}
				}
			}
		});
		warningColor = new Color(parent.getDisplay(), WARNING_COLOR);
		new GreyedText(text, "Type addresses, domains or URLs here ...");
		return text;
	}

	protected int computeWidth(Control control) {
		return 300;
	}
	
	boolean actionEnabled() {
		return buttonAction.isEnabled();
	}
	
	public void runAction() {
		if (!actionEnabled()) return;
		final ISpace space = getActiveSpace();
		if(space == null) {
			return;
		}
		logger.info("run: "+content+", action="+availableActions.get(0).getText() + " space= " + space.getId());
		IAction action = availableActions.get(0);
	
		if(action instanceof ISpaceAction) {
			((ISpaceAction)action).setSpace(space);
		}
		action.run();

		if (space.getName().matches("Space \\d+")) {
			space.setName(content);
		}

		content = "";
		text.setText(content);
		update();
	}
	
	private ISpace getActiveSpace() {
		IEditorPart editor = Activator.getDefault().getActiveEditor();
		if(editor == null) {
			return null;
		}
		if(!(editor.getEditorInput() instanceof SpaceEditorInput)) {
			return null;
		}
		return ((SpaceEditorInput)editor.getEditorInput()).getSpace();
	}
	
	private void doUpdate() {
		if (content.length() == 0) {
			availableActions = new ArrayList<IAction>();
		} else {
			//long realm = Activator.getDefault().getProbeManager().getLocalProbe().getEntity().getId();
			ISpace space = getActiveSpace();
			if(space == null) return;
			
			IProbe probe = Activator.getDefault().getProbeManager().getProbeById(space.getProbeId());
			if(probe == null) {
				logger.warning("No probe found for probe id = " + space.getProbeId());
				setActionEnabled(false);
				return;
			}
		
			final IInputBarActionProviderService actionProvider = Activator.getDefault().getInputBarActionProvider();
			long realm = probe.getEntity().getId();
			availableActions = actionProvider.getActions(realm, space.getId(), content);
		}
		setActionEnabled(canRun());
	}
	
	private boolean canRun() {
		return availableActions.size() > 0;
	}
	
	private void setActionEnabled(boolean enabled) {
		buttonAction.setEnabled(enabled);
	}
}
