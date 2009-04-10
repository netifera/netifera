package com.netifera.platform.ui.tasks.list;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.netifera.platform.api.probe.IProbe;
import com.netifera.platform.api.tasks.ITaskClient;
import com.netifera.platform.api.tasks.ITaskRecord;
import com.netifera.platform.ui.internal.tasks.TasksPlugin;
import com.netifera.platform.ui.internal.tasks.util.ProgressBarStack;
import com.netifera.platform.ui.tasks.output.TaskOutputView;

public class TaskItem extends Composite {

	private static final String STOP_TASK_IMAGE = "icons/stop_task.png";
	private static final String STOP_TASK_GRAY_IMAGE = "icons/stop_task_gray.png";

	private Composite titleComposite;
	private Label titleImage;
	private Label titleLabel;
	
	private Composite progressComposite;
	private ProgressBarStack progress;
	private ImageHyperlink cancelButton;
	private boolean cancelDisabled;

//	private ImageHyperlink messageLink;

	private Composite statusComposite;
	private Label statusLabel;
	private ImageHyperlink outputLink;
	
	private TaskLabelProvider labelProvider = new TaskLabelProvider();
//	private TaskOutputTableLabelProvider outputLabelProvider = new TaskOutputTableLabelProvider();
	
	public TaskItem(final Composite parent, int style, FormToolkit toolkit) {
		super(parent, style);

		final TableWrapLayout layout = new TableWrapLayout();
		setLayout(layout);
		toolkit.adapt(this);
		toolkit.paintBordersFor(this);

		createTitle(toolkit, style);
		createProgress(toolkit);
//		createMessage(toolkit);
		createStatus(toolkit);

		addHoverHandler();
		
		this.layout(true);
		//this.setLayoutDeferred(true);
	}

	private void createTitle(FormToolkit toolkit, int style) {
		titleComposite = toolkit.createComposite(this, style);
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
		titleComposite.setLayoutData(layoutData);
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.leftMargin = layout.rightMargin = layout.topMargin = layout.bottomMargin = 0;
		titleComposite.setLayout(layout);
		
		titleImage = toolkit.createLabel(titleComposite, "", style);
		layoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		layoutData.colspan = 1;
		titleImage.setLayoutData(layoutData);
		
		titleLabel = toolkit.createLabel(titleComposite, "Untitled Task", SWT.WRAP);
		titleLabel.setFont(getTitleFont());
		layoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		layoutData.colspan = 1;
		titleLabel.setLayoutData(layoutData);
	}
	
	private Font getTitleFont() {
		FontRegistry fontRegistry = PlatformUI.getWorkbench().getThemeManager()
		.getCurrentTheme().getFontRegistry();
		/* default font with bold style, could be defined in a theme */
		Font titleFont = fontRegistry.getBold(JFaceResources.DEFAULT_FONT);
		return titleFont;
	}
	
	private void createProgress(FormToolkit toolkit) {
		progressComposite = toolkit.createComposite(this);
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		progressComposite.setLayoutData(layoutData);
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.leftMargin = layout.rightMargin = layout.topMargin = layout.bottomMargin = 0;
		progressComposite.setLayout(layout);
		
		progress = new ProgressBarStack(progressComposite);
		layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
		layoutData.heightHint = 16;
		layoutData.grabHorizontal = true;
		progress.setLayoutData(layoutData);
		toolkit.adapt(progress);

		cancelButton = toolkit.createImageHyperlink(progressComposite, SWT.NONE);
		cancelButton.setToolTipText("Cancel Task");
		cancelButton.setImage(TasksPlugin.getPlugin().getImageCache().get(STOP_TASK_IMAGE));
		layoutData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);
		layoutData.grabHorizontal = false;
		layoutData.maxWidth = 20;
		cancelButton.setLayoutData(layoutData);
		
		cancelButton.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				doCancel();
			}
		});
	}

/*	private void createMessage(FormToolkit toolkit) {
		messageLink = toolkit.createImageHyperlink(this, SWT.NONE);
		messageLink.setFont(JFaceResources.getDialogFont());
		messageLink.setText("");
		messageLink.setForeground(getForeground());
		messageLink.setUnderlined(false);
		messageLink.setVisible(false);

		messageLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doOpenOutput();
			}
		});
	}

	private void handleTaskOutput(TaskLogOutput log) {
		if (messageLink == null || messageLink.isDisposed())
			return;
		messageLink.setText(outputLabelProvider.getColumnText(log, 1));
		if (log.isInfo())
			messageLink.setImage(null);
		else
			messageLink.setImage(outputLabelProvider.getColumnImage(log, 1));
	}
*/
	private void createStatus(FormToolkit toolkit) {
		statusComposite = toolkit.createComposite(this);
		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		statusComposite.setLayoutData(layoutData);
		final TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.leftMargin = layout.rightMargin = layout.topMargin = layout.bottomMargin = 0;
		statusComposite.setLayout(layout);
		
		statusLabel = toolkit.createLabel(statusComposite, "Unknown status");
		statusLabel.setFont(JFaceResources.getDialogFont());
		layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
		statusLabel.setLayoutData(layoutData);
		
		outputLink = toolkit.createImageHyperlink(statusComposite, SWT.NONE);
//		outputSection.setFont(titleLabel.getFont());
		outputLink.setFont(JFaceResources.getDialogFont());
		outputLink.setText("details..");
		layoutData = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		outputLink.setLayoutData(layoutData);

		outputLink.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				doOpenOutput();
			}
		});
	}
	
	@Override
	public void setData(Object data) {
		super.setData(data);
		if(data instanceof ITaskRecord) {
			ITaskRecord taskRecord = (ITaskRecord) data;
			
			String title = taskRecord.getTitle();
			if (title != null && !titleLabel.isDisposed())
				titleLabel.setText(title);

			titleImage.setImage(labelProvider.getImage(taskRecord));
		
			String status = taskRecord.getStatus();
			if (status == null)
				status = taskRecord.getStateDescription();
			if (taskRecord.isFinished())
				status = "Completed";
			if (taskRecord.isFinished() || taskRecord.isFailed()) {
				status = status+" ("+labelProvider.getElapsedTime(taskRecord)+")";
				
/*				List<ITaskOutput> output = taskRecord.getTaskOutput();
				for (int i=output.size()-1; i>=0; i--) {
					if (output.get(i) instanceof TaskLogOutput) {
						handleTaskOutput((TaskLogOutput) output.get(i));
//						outputLink.setVisible(false);
						break;
					}
				}
*/			}
			
			if (!statusLabel.isDisposed()) {
				statusLabel.setText(status);
			}
			
/*			if(taskRecord.isRunning()) {
				enableCancel();
			} else {
				disableCancel();
			}
*/			
			if (taskRecord.isFinished() || taskRecord.isFailed()) {
				if (!progress.isDisposed())
					progress.dispose();
				if (!cancelButton.isDisposed())
					cancelButton.dispose();
				if (!progressComposite.isDisposed()) {
					progressComposite.setVisible(false);
					progressComposite.dispose();
//					messageLink.setVisible(true);
				}
			} else {
				int worked = taskRecord.getWorkDone();
				if (worked > 0 && !progress.isDisposed()) {
					progress.setSelection(taskRecord.getWorkDone());
				}
			}
		}
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		if (titleComposite == null) return; // still not initialized
		if (!titleComposite.isDisposed())
			titleComposite.setBackground(color);
		if (!titleImage.isDisposed())
			titleImage.setBackground(color);
		if (!titleLabel.isDisposed())
			titleLabel.setBackground(color);
		if (!progressComposite.isDisposed())
			progressComposite.setBackground(color);
		if (!progress.isDisposed()) {
			progress.setBackground(color);
			cancelButton.setBackground(color);
		}
//		if (!messageLink.isDisposed())
//			messageLink.setBackground(color);
		if (!statusComposite.isDisposed())
			statusComposite.setBackground(color);
		if (!statusLabel.isDisposed())
			statusLabel.setBackground(color);
		if (!outputLink.isDisposed())
			outputLink.setBackground(color);
	}
	
	@Override
	public void addMouseListener(MouseListener listener) {
		super.addMouseListener(listener);
		if (titleComposite == null) return; // still not initialized
		if (!titleComposite.isDisposed())
			titleComposite.addMouseListener(listener);
		if (!titleImage.isDisposed())
			titleImage.addMouseListener(listener);
		if (!titleLabel.isDisposed())
			titleLabel.addMouseListener(listener);
		if (!progressComposite.isDisposed())
			progressComposite.addMouseListener(listener);
		if (!progress.isDisposed()) {
			progress.addMouseListener(listener);
		}
		if (!statusComposite.isDisposed())
			statusComposite.addMouseListener(listener);
		if (!statusLabel.isDisposed())
			statusLabel.addMouseListener(listener);
	}

	@Override
	public void dispose() {
		titleComposite.dispose();
		titleImage.dispose();
		titleLabel.dispose();
		progressComposite.dispose();
		progress.dispose();
		cancelButton.dispose();
//		messageLink.dispose();
		statusComposite.dispose();
		statusLabel.dispose();
		outputLink.dispose();
		super.dispose();
	}
	
/*	private void disableCancel() {
		if(cancelDisabled || cancelButton.isDisposed()) {
			return;
		}
		cancelButton.setImage(TasksPlugin.getPlugin().getImageCache().get(STOP_TASK_GRAY_IMAGE));
		cancelDisabled = true;
	}
	
	private void enableCancel() {
		if(!cancelDisabled || cancelButton.isDisposed()) {
			return;
		}
		cancelButton.setImage(TasksPlugin.getPlugin().getImageCache().get(STOP_TASK_IMAGE));
		cancelDisabled = false;
	}*/
	
	private void doCancel() {
		if(cancelDisabled) return;
		ITaskRecord record = (ITaskRecord) getData();
		if (record == null) return;
		
		IProbe probe = TasksPlugin.getPlugin().getProbeManager().getProbeById(record.getProbeId());
		ITaskClient client = probe.getTaskClient();
		client.requestCancel(record.getTaskId());
	}
	
	private void doOpenOutput() {
		if (!(getData() instanceof ITaskRecord))
			return;
		ITaskRecord task = (ITaskRecord) getData();
			
		try {
			IViewPart view;
			view = TasksPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					TaskOutputView.ID,
					"Task-"+task.getTaskId(),
					IWorkbenchPage.VIEW_ACTIVATE);
			((TaskOutputView)view).setInput(task);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addHoverHandler() {
		MouseTrackAdapter listener = new MouseTrackAdapter() {
			public void mouseHover(MouseEvent e) {
				final Point p = PlatformUI.getWorkbench().getDisplay().map(TaskItem.this, null, e.x, e.y);
				new TaskHover(TaskItem.this.getShell(), p, (ITaskRecord)TaskItem.this.getData()).open();
			}
		};

//		this.addMouseTrackListener(listener);
//		titleComposite.addMouseTrackListener(listener);
//		titleImage.addMouseTrackListener(listener);
//		titleLabel.addMouseTrackListener(listener);
//		progressComposite.addMouseTrackListener(listener);
//		progress.addMouseTrackListener(listener);
//		statusComposite.addMouseTrackListener(listener);
//		statusLabel.addMouseTrackListener(listener);
		outputLink.addMouseTrackListener(listener);
//		messageLink.addMouseTrackListener(listener);
	}
}
