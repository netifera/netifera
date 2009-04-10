package com.netifera.platform.ui.tasks.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ColumnLayout;

/**
 * Viewer to show a collection of elements in a ScrolledComposite with any custom widget
 * to show each item. Extends StructuredViewer
 *
 */

public class ItemViewer extends StructuredViewer {
	static String DARK_COLOR = "com.netifera.platform.ui.DARK_COLOR_LIST";
	 static {
		// Mac has different Gamma value
		int shift = "carbon".equals(SWT.getPlatform()) ? -25 : -10;//$NON-NLS-1$ 

		final Color lightColor = PlatformUI.getWorkbench().getDisplay()
				.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		// Determine a dark color by shifting the list color
		RGB darkRGB = new RGB(Math.max(0, lightColor.getRed() + shift), Math
				.max(0, lightColor.getGreen() + shift), Math.max(0, lightColor
				.getBlue()
				+ shift));
		JFaceResources.getColorRegistry().put(DARK_COLOR, darkRGB);

	}

	/* map between data elements and UI items widgets */
	private Map<Object,Widget> itemMap = new ConcurrentHashMap<Object,Widget>();

	/* provides UI widgets to represent elements */
	private IItemProvider itemProvider;
//	private List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();
	private List<Object> selection = new ArrayList<Object>();

	private Composite control;
	private ScrolledComposite scrolled;
	private volatile boolean busy;

	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public ItemViewer(Composite parent, int style) {
		int height = JFaceResources.getDefaultFont().getFontData()[0].getHeight();
		scrolled = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		scrolled.setLayout(new FillLayout());
		control = new Composite(scrolled, SWT.NONE);
		control.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		scrolled.setContent(control);
		ColumnLayout columnLayout = new ColumnLayout();
		/* number of columns in the form, set to one to avoid composites side by side  */
		columnLayout.maxNumColumns = 1;
		columnLayout.minNumColumns = 1;
		columnLayout.horizontalSpacing = 1;
		columnLayout.verticalSpacing = 0;
		columnLayout.leftMargin = 0;
		columnLayout.rightMargin = 0;
		columnLayout.topMargin = 0;
		columnLayout.bottomMargin = 0;
		control.setLayout(columnLayout);
//		toolkit.paintBordersFor(control);

	    scrolled.addControlListener(new ControlAdapter() {
	      public void controlResized(ControlEvent e) {
	        Rectangle r = scrolled.getClientArea();
	        scrolled.setMinSize(control.computeSize(r.width,
	            SWT.DEFAULT));
	      }
	    });
	    
		scrolled.getVerticalBar().setIncrement(height * 2);		
		scrolled.setExpandHorizontal(true);	
		scrolled.setExpandVertical(true);
	}

	public void dispose() {
		/*XXX dispose widgets ? */

		if(itemProvider != null) {
			itemProvider.dispose();
			itemProvider = null;
		}
	}
	
	/**
	 * Returns a UI widget for the given model element using the UI item provider
	 * 
	 * @param element data object from model
	 */
	private Widget createItemWidget (Object  element) {
		
		if(itemProvider == null) {
			return null;
		}
		
		/* call item provider to create the widget */
		Widget item = itemProvider.getItem(element);

		if(item == null) {
			return null;
		}

		final Object finalElement = element;
		final Control itemControl = ((Control)item);		
		
		/*add listener to set the selection when the widget is focused */
		
		((Control)item).addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if(e.widget.getData() != null) {
					setSelection(new StructuredSelection(e.widget.getData()),true);
				}
				else {
					setSelection(new StructuredSelection(finalElement),true);
				}
			}
			public void focusLost(FocusEvent e) {

			}
		});
		
		
		/* set focus when the item is clicked, to trigger selection  */
		MouseListener mouseListener = new MouseListener() {
			public void mouseDown(MouseEvent e) {
				itemControl.forceFocus();
			}
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {	}
			
		};
		
		itemControl.addMouseListener(mouseListener);		
		
		/*add the new item to the map*/
		itemMap.put(element,item);
		
		return item;
	}

	/**
	 * Updates the UI item for the given element 
	 * @param element not null
	 */
	protected synchronized void internalRefresh(Object element) {
		
		Assert.isNotNull(itemProvider);
		if(control.isDisposed()) {
			return;
		}
		
		/* if element is null or is the input/root item then  refresh all */
		if(element == null || element.equals(getRoot())) {
			internalRefreshAll();
			return;
		}
		
		if(itemMap.containsKey(element)) {
			Widget item = itemMap.get(element);
			if(!item.isDisposed()) {
				doUpdateItem(item,element,true);
			}
			else {
				/* widget disposed what to do? remove from map? or nothing */
				itemMap.remove(element);
			}
		} else 
			/* new element, create item and set it as new selection*/
		{
			/* if the items are sorted we have to create all the widgets again */
			//TODO: is possible to avoid it?
			if(this.getComparator() != null) {
				internalRefreshAll();
			} else {
				/*TODO itemProvider could return a hint if layout again is necessary or not */
				Widget item  = createItemWidget(element);
				((Control)item).setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
//				control.layout(true);
			}
			setSelection(new StructuredSelection(element),true);
		}
	}

	/**
	 * Create widgets from the current input, existing widgets are disposed. 
	 */
	private void internalRefreshAll() {

		if(isBusy() || control.isDisposed() || getInput() == null || getContentProvider() == null) {
			return;
		}
		
		setBusy(true);
		control.setRedraw(false);
		control.getParent().setRedraw(false);

		/* dispose current item widgets saving their state */
		for(Object element : itemMap.keySet()) {
			Widget item = itemMap.get(element);
			item.dispose();
			itemMap.remove(element);
		}
		
		Object[] elements = getSortedChildren(getInput());
		assertElementsNotNull(elements);
		
		/* create a new widget for each element and set saved state */
		int i = elements.length%2;
		for(Object element : elements) {
			Widget item = createItemWidget(element);
			((Control)item).setBackground(getItemBackgroundColor(i));
			i++;
		}

		control.layout(true);
        scrolled.setMinSize(control.computeSize(scrolled.getClientArea().width, SWT.DEFAULT));
        

		/* update size and position of controls */
		control.setRedraw(true);
		control.getParent().setRedraw(true);
		setBusy(false);
	}
	private Color getItemBackgroundColor(int i) {
		if(i%2 == 0) {
			return Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);			
		} else {
			return JFaceResources.getColorRegistry().get(DARK_COLOR);
		}
	}
	protected boolean isBusy() {
		return busy;
	}

	protected void setBusy(boolean busy) {
		this.busy = busy;
	}
	
	public IItemProvider getItemProvider() {
		return itemProvider;
	}

	/** 
	 * Sets the viewer label provider, if the item provider is set it is also set for it. 
	 */
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ILabelProvider);
		super.setLabelProvider(labelProvider);
		if(itemProvider != null) {
			itemProvider.setLabelProvider((ILabelProvider)labelProvider);
		}
	}
	
	public void setItemProvider(IItemProvider itemProvider) {
		itemProvider.setParent(control);
		this.itemProvider = itemProvider;
		if(getLabelProvider() != null) {
			itemProvider.setLabelProvider((ILabelProvider)getLabelProvider());
		}
	}
	
	/** StructuredViewer inputChanged "hook" method. 
	 * Remove previous items and clear selection */
	protected void inputChanged(Object input, Object oldInput) {

		if(input == oldInput) {
			return;
		}
		
		/* empty selection */
		selection.clear();
		setSelection(StructuredSelection.EMPTY);
		
		/* dispose current item widgets */		
		for(Object element : itemMap.keySet()) {
			itemMap.get(element).dispose();
			itemMap.remove(element);
		}

		//FIX?
		internalRefreshAll();
    }

	public Control getControl() {
		return control;
	}

	public void update(Object element, String[] properties) {
		refresh(element);
	}
	
	/** Selection related methods */
	private void setSelectionInternal(Object element, boolean reveal) {
		if(element != null && itemMap.containsKey(element)) {
			Widget item = itemMap.get(element);
			if(reveal && item instanceof Control && !item.isDisposed()) {
				((Control)item).setFocus();
			}
			selection.clear();
			selection.add(element);
		}
	}

	/**
	 * Searches for the specified data object and scroll the list to show it if necessary
	 * @param element data object to search and show.
	 */
	public void reveal(Object element) {
		setSelectionInternal(element,true);
	}

	@SuppressWarnings("unchecked")
	protected List getSelectionFromWidget() {
		return selection;
	}

	@SuppressWarnings("unchecked")
	protected void setSelectionToWidget(List list, boolean reveal) {
		if(list.isEmpty()) {
			return;
		}
		//TODO change this code to allow multiple selection
		setSelectionInternal(list.get(0),reveal);
	}

	protected Widget doFindInputItem(Object element) {
		return null;
	}

	protected Widget doFindItem(Object element) {
		return itemMap.containsKey(element) ? itemMap.get(element) : null;
	}

	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		itemProvider.updateItem(item, element);
		control.layout(true,true);
/*		control.layout(new Control[]{(Control)item});
		
		//TODO: check if item size changed and update scrollable height, does the following computeSize code work?
		Control ci = (Control)item;
		Point s1 = ci.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		itemProvider.updateItem(item, element);
		Point s2 = ci.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if(!s1.equals(s2)) {
//			control.layout(new Control[]{(Control)item});
			control.layout(true);
		}
*/	}
}
