package com.netifera.platform.ui.spaces.actions;

import java.util.Set;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.ui.internal.spaces.Activator;

public class AddTagDialog extends PopupDialog {
	
	private FormToolkit toolkit;
	private Form form;
	private Composite body;

	private final ISpace space;
	private final IShadowEntity entity;

	private CCombo combo;

	private Image addImage = Activator.getDefault().getImageCache().get("icons/add.png");
	private ImageHyperlink addLink;
	
	public AddTagDialog(Shell parent, Point location, ISpace space, IShadowEntity entity) {
		super(parent, PopupDialog.INFOPOPUP_SHELLSTYLE | SWT.ON_TOP, true, false, false, false, false, null, "Press ESC to exit");
		this.space = space;
		this.entity = entity;
		
		create();
		getShell().setLocation(location);

		setHeader();
		addOptions();
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		composite.setLayout(new FillLayout());
		
		toolkit = new FormToolkit(composite.getDisplay());
		form = toolkit.createForm(composite);
		
		FormColors colors = toolkit.getColors();
		colors.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		
//		toolkit.getHyperlinkGroup().setActiveForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
//		toolkit.getHyperlinkGroup().setForeground(colors.getColor("Categorytitle"));
		toolkit.getHyperlinkGroup().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		body = form.getBody();
		body.setLayout(new GridLayout());
//		body.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolkit.paintBordersFor(body);

		return composite;
	}

	private void setHeader() {
		form.setFont(JFaceResources.getDialogFont());
		
		Image icon = Activator.getDefault().getImageCache().get("icons/tag_blue_add_16x16.png");
		form.setImage(icon);
		
		String text = Activator.getDefault().getLabelProvider().getText(entity);
		form.setText("Tag "+text);
		
		form.setSeparatorVisible(true);
		
		toolkit.decorateFormHeading(form);
	}

	private void addOptions() {
		combo = new CCombo(body, SWT.NONE);
		combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
		gd.widthHint = 200;
		combo.setLayoutData(gd);
		combo.setEditable(true);
		combo.setToolTipText("Add Tag");
		combo.setText("");

		Set<String> entityTags = entity.getRealEntity().getTags();
		Set<String> spaceTags = space.getTags();
		if (!entityTags.contains("Target"))
			combo.add("Target"); // at index 0
		for (String tag: spaceTags) {
			if (!tag.equals("Target") && !entityTags.contains(tag))
				combo.add(tag);
		}

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateAddButton();
			}
		});
		combo.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				updateAddButton();
				if (e.character == SWT.CR)
					doAddTag();
			}
		});

		addLink = toolkit.createImageHyperlink(body, SWT.NONE);
//		addLink.setFont(JFaceResources.getHeaderFont());
		addLink.setImage(addImage);
		addLink.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true));
		addLink.setText("Add");
		updateAddButton();

		addLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doAddTag();
			}
		});
	}

	private void updateAddButton() {
		if (!isValidTag(combo.getText())) {
			addLink.setVisible(false);
			addLink.setEnabled(false);
		} else {
			addLink.setEnabled(true);
			addLink.setVisible(true);
		}
	}
	
	private boolean isValidTag(String tag) {
		if (entity.getTags().contains(tag))
			return false;
		return tag.length() > 0;
	}
	
	private void doAddTag() {
		if (!addLink.isEnabled())
			return;
		final AbstractEntity realEntity = (AbstractEntity) entity.getRealEntity();
		realEntity.addTag(combo.getText());
		realEntity.update();
		close();
	}
	
	@Override
	protected void adjustBounds() {
		getShell().pack();
//		Point size = getShell().getSize();
//		size.x = ToolPanel.PANEL_PIXEL_WIDTH;
//		getShell().setSize(size);
	}
	
	@Override
	protected Control getFocusControl() {
		return combo;
	}

	@Override
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();
		toolkit.dispose();
	}
}