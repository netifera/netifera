package com.netifera.platform.ui.tasks.list;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.tasks.output.TaskOutputTableViewer;

public class TaskHover extends PopupDialog {
	
	private FormToolkit toolkit;
	private Form form;
	private Composite body;

	private final ITaskRecord task;
	private TaskOutputTableViewer viewer;
	private TaskLabelProvider labelProvider = new TaskLabelProvider();
	
	public TaskHover(Shell parent, Point location, ITaskRecord task) {
		super(parent, PopupDialog.INFOPOPUP_SHELLSTYLE | SWT.ON_TOP, true, false, false, false, false, null, "Press 'ESC' to exit");
		this.task = task;
		
		create();
		getShell().setLocation(location);

		setHeader();
		addViewer();
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
//		body.setLayout(new GridLayout());
//		body.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
		layoutData.maxHeight = 150;
		body.setLayoutData(layoutData);
		body.setLayout(new TableWrapLayout()); //XXX

		toolkit.paintBordersFor(body);

		return composite;
	}

	private void setHeader() {
		form.setFont(JFaceResources.getDialogFont());
		form.setImage(labelProvider.getImage(task));
		form.setText(task.getTitle());
		
		form.setSeparatorVisible(true);
		
		toolkit.decorateFormHeading(form);
	}

	private void addViewer() {
		viewer = new TaskOutputTableViewer(body, body.getStyle(), true);
		viewer.setInput(task);
		
		Control viewerControl = viewer.getControl();
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
		int h = viewerControl.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		layoutData.maxHeight = h*8;
		viewerControl.setSize(SWT.DEFAULT, layoutData.maxHeight);
		layoutData.heightHint = layoutData.maxHeight;
		viewerControl.setLayoutData(layoutData);
		
		form.layout(true);
	}

	@Override
	protected void adjustBounds() {
		getShell().pack();
/*		Point size = getShell().getSize();
		size.x = 400;
		getShell().setSize(size);
*/	}
	
	@Override
	protected Control getFocusControl() {
		return viewer.getControl();
	}
	
	protected void handleShellCloseEvent() {
		super.handleShellCloseEvent();
		toolkit.dispose();
		if (!viewer.getControl().isDisposed())
			viewer.getControl().dispose();
	}

}
