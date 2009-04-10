package com.netifera.platform.ui.spaces.actions;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.kernel.tools.ToolConfiguration;
import com.netifera.platform.model.FolderEntity;
import com.netifera.platform.ui.actions.SpaceAction;
import com.netifera.platform.ui.api.actions.ISpaceAction;
import com.netifera.platform.ui.internal.spaces.Activator;

public class ActionHover extends PopupDialog {
	private final IShadowEntity entity;
	private ISpace space;

	private FormToolkit toolkit;
	private Form form;
	private Composite body;
	
	private Color background;
	private Color darkerBackground;
	
	private Hyperlink firstActionLink;
	private Point location;

	public ActionHover(Shell parent, Point location, Object input, Object item) {
		super(parent, PopupDialog.INFOPOPUP_SHELLSTYLE | SWT.ON_TOP , false, false, false, false, false, 
				/*ModelPlugin.getPlugin().getLabelProvider().getText(entity.getRealEntity())*/ null, null);
		if(!(input instanceof ISpace))
			throw new IllegalArgumentException("ActionHover input is "+input);
		if(!(item instanceof IShadowEntity))
			throw new IllegalArgumentException("ActionHover item is "+item);
		this.location = location;
		this.entity = (IShadowEntity)item;
		this.space = (ISpace) input;
		show();
	}

	private void show() {
		create();
		getShell().setLocation(location);

		setHeader();
		addInformation();
		if(getCurrentProbe().isConnected()) {
			addActions();
		} else {
			addProbeConnectAction();
		}
	}
	
	private IProbe getCurrentProbe() {
		return Activator.getDefault().getProbeManager().getProbeById(space.getProbeId());
	}
	
	private void addProbeConnectAction() {
		toolkit.createLabel(body, "The probe for this space is currently disconnected so no actions are available.");
		final ImageHyperlink link = toolkit.createImageHyperlink(body, SWT.NONE);
		link.setImage(Activator.getDefault().getImageCache().get("icons/action.png"));
		link.setText("Connect To Probe");
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				getCurrentProbe().connect();
				close();
			}
		});
		
		firstActionLink = link;
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		composite.setLayout(new FillLayout());
		
		toolkit = new FormToolkit(composite.getDisplay());
		form = toolkit.createForm(composite);
		
		FormColors colors = toolkit.getColors();
		
		background = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		colors.setBackground(background);

		float[] hsb = background.getRGB().getHSB();
		darkerBackground = colors.createColor("darkerBackground", new RGB(hsb[0], hsb[1]*(float)1.1, hsb[2]*(float)0.92));
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		
//		toolkit.getHyperlinkGroup().setActiveForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
//		toolkit.getHyperlinkGroup().setForeground(colors.getColor("Categorytitle"));
		toolkit.getHyperlinkGroup().setBackground(background);

		body = form.getBody();
		body.setLayout(new GridLayout());
		toolkit.paintBordersFor(body);

		return composite;
	}

	private void setHeader() {
		form.setFont(JFaceResources.getDefaultFont());
		
		Image icon = Activator.getDefault().getLabelProvider().getImage(entity);
		form.setImage(icon);
		
		String text = Activator.getDefault().getLabelProvider().getText(entity);
		form.setText(text);
		
		form.setSeparatorVisible(true);
		
		toolkit.decorateFormHeading(form);
	}

	private void addSeparator() {
		Label separator = toolkit.createSeparator(body, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	private void addInformation() {
		if (!(entity instanceof AbstractEntity)) return;
		Set<String> tags = ((AbstractEntity)entity.getRealEntity()).getTags();
		if (tags.size() > 0) {
			Composite tagsArea = toolkit.createComposite(body, SWT.NONE);
			RowLayout layout = new RowLayout();
			layout.wrap = true;
			tagsArea.setLayout(layout);
			for (String tag: tags)
				addTag(tagsArea, tag);
		}

		String information = Activator.getDefault().getLabelProvider().getInformation(entity);
		if (information != null && information.length()>0) {
			FormText informationForm = toolkit.createFormText(body, true);
			informationForm.setFont(JFaceResources.getDefaultFont());
			informationForm.setColor("red", Display.getDefault().getSystemColor(SWT.COLOR_RED));
			informationForm.setColor("green", Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
			informationForm.setColor("blue", Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
			informationForm.setParagraphsSeparated(false);
			informationForm.setText("<form>"+information+"</form>", true, false);
			
			addSeparator();
		} else {
			if (tags.size() > 0)
				addSeparator();
		}
	}

	private void addTag(Composite parent, final String tag) {
		final AbstractEntity realEntity = (AbstractEntity) entity.getRealEntity();
		final Composite c = toolkit.createComposite(parent, SWT.BORDER);
		c.setBackground(darkerBackground);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		layout.marginHeight = 1;
		layout.marginWidth = 2;
		c.setLayout(layout);
		
		Label label = toolkit.createLabel(c, tag);
		label.setBackground(darkerBackground);
		
		ImageHyperlink link = toolkit.createImageHyperlink(c, SWT.NONE);
		link.setBackground(darkerBackground);
		link.setImage(Activator.getDefault().getImageCache().get("icons/delete.png"));
		link.setHoverImage(Activator.getDefault().getImageCache().get("icons/delete_hover.png"));
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		link.setLayoutData(data);
		link.setToolTipText("Remove Tag '"+tag+"'");
		
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				realEntity.removeTag(tag);
				realEntity.update();
				close();
			}
		});
	}
	
	private void addActions() {
		try {
			List<IAction> actions = Activator.getDefault().getActionProvider().getActions(entity);
//			toolkit.createLabel(body, actions.size()==0 ? "No actions available for this entity." : actions.size()+(actions.size()==1 ? " action":" actions")+" available:");
			for (IAction action: actions)
				addAction(action, body, false);

			List<IAction> quickActions = Activator.getDefault().getActionProvider().getQuickActions(entity);
			if (!(entity instanceof FolderEntity)) {
				IAction addTagAction = new SpaceAction("Add Tag") {
					public void run() {
						AddTagDialog addTagDialog = new AddTagDialog(getParentShell(), getShell().getLocation(), space, entity);
						addTagDialog.open();
						ActionHover.this.close();
					}
				};
				addTagAction.setImageDescriptor(Activator.getDefault().getImageCache().getDescriptor("icons/tag_blue_add_16x16.png"));
				quickActions.add(addTagAction);
				
				IAction removeAction = new SpaceAction("Remove Entity From This Space") {
					public void run() {
						getSpace().removeEntity(entity.getRealEntity());
					}
				};
				removeAction.setImageDescriptor(Activator.getDefault().getImageCache().getDescriptor("icons/delete_hover.png"));
				quickActions.add(removeAction);
			}
			if (quickActions.size()>0) {
				addSeparator();
				Composite bar = toolkit.createComposite(body);
				bar.setLayout(new GridLayout(20,false));
				for (IAction action: quickActions)
					addAction(action, bar, true);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

/*	private void addShadowActions() {
		try {
			List<IAction> actions = Activator.getDefault().getActionProvider().getActions(entity);
			if (actions.size() == 0) return;
			addSeparator();
			toolkit.createLabel(body, actions.size()+(actions.size()==1 ? " structural action":" structural actions")+" available:");
			for (IAction action: actions)
				addAction(action);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
*/	
	private void addArrowNavigation(Control c) {
		c.addKeyListener(new KeyListener(){ 
			public void keyPressed(KeyEvent e) {
				Control control = (Control)e.widget;
				if(e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_RIGHT) {
				 control.traverse(SWT.TRAVERSE_TAB_NEXT);
				}
				else if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_LEFT) {
					control.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
				}
			}
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	private void addAction(final IAction action, Composite parent, boolean isQuick) {
		final ImageHyperlink link = toolkit.createImageHyperlink(parent, SWT.NONE);
		if (action.getImageDescriptor() != null) {
			link.setImage(action.getImageDescriptor().createImage());
		} else {
			link.setImage(Activator.getDefault().getImageCache().get("icons/action.png"));
		}

		if (isQuick)
			link.setToolTipText(action.getText());
		else
			link.setText(action.getText());
		
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				if (action instanceof ISpaceAction) {
					ISpaceAction spaceAction = (ISpaceAction) action;
					spaceAction.setSpace(space);
					if (((ToolConfiguration) spaceAction.getConfiguration()).isFixed()) {
						spaceAction.run();
					} else {
						RunActionDialog runActionDialog = new RunActionDialog(getParentShell(), getShell().getLocation(), spaceAction);
						runActionDialog.open();
					}
				} else {
					action.run();
				}
				close();
			}
		});
		
		if (firstActionLink == null)
			firstActionLink = link;
		addArrowNavigation(link);
	}
	
	protected void adjustBounds() {
		getShell().pack();
	}
	
	protected Control getFocusControl() {
		return firstActionLink == null ? form : firstActionLink;
	}

	@Override
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();
		toolkit.dispose();
	}
}
