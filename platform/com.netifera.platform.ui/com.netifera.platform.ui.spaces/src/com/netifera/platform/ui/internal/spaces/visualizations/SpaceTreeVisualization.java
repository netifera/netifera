package com.netifera.platform.ui.internal.spaces.visualizations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.api.model.layers.ILayerProvider;
import com.netifera.platform.api.model.layers.ITreeLayerProvider;
import com.netifera.platform.model.FolderEntity;
import com.netifera.platform.model.TreeStructureContext;
import com.netifera.platform.ui.api.actions.ISpaceAction;
import com.netifera.platform.ui.api.inputbar.IInputBarActionProviderService;
import com.netifera.platform.ui.dnd.EntityTransfer;
import com.netifera.platform.ui.internal.spaces.Activator;
import com.netifera.platform.ui.spaces.actions.ActionHover;
import com.netifera.platform.ui.spaces.actions.SelectLayersAction;
import com.netifera.platform.ui.spaces.tree.SpaceTreeContentProvider;
import com.netifera.platform.ui.spaces.tree.SpaceTreeLabelProvider;
import com.netifera.platform.ui.spaces.tree.TreeBuilder;
import com.netifera.platform.ui.spaces.tree.TreeViewerComparator;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualization;
import com.netifera.platform.ui.util.MouseTracker;
import com.netifera.platform.ui.util.TreeAction;

public class SpaceTreeVisualization implements ISpaceVisualization {
	
	final private ISpace space;
	private TreeViewer viewer;
	private SpaceTreeContentProvider contentProvider;
	
	public SpaceTreeVisualization(ISpace space) {
		this.space = space;
	}

	public void addContributions(IContributionManager contributions) {
		contributions.add(new SelectLayersAction() {
			@Override
			protected void disableLayer(ILayerProvider provider) {
				contentProvider.removeLayer(provider);
			}
			@Override
			protected void enableLayer(ILayerProvider provider) {
				contentProvider.addLayer(provider);
			}
			@Override
			protected List<ILayerProvider> getActiveLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: contentProvider.getLayers()) {
					if (layerProvider instanceof ITreeLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
			@Override
			protected List<ILayerProvider> getLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
					if (layerProvider instanceof ITreeLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
		});

		contributions.add(new SelectLayersAction("Select Folders", Activator.getDefault().getImageCache().getDescriptor("icons/folders.png")) {
			@Override
			protected void disableLayer(ILayerProvider provider) {
				contentProvider.removeLayer(provider);
			}
			@Override
			protected void enableLayer(ILayerProvider provider) {
				contentProvider.addLayer(provider);
			}
			@Override
			protected List<ILayerProvider> getActiveLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: contentProvider.getLayers()) {
					if (layerProvider instanceof IGroupLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
			@Override
			protected List<ILayerProvider> getLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
					if (layerProvider instanceof IGroupLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
		});

		contributions.add(TreeAction.collapseAll(viewer));
		contributions.add(TreeAction.expandAll(viewer));
	}

	public ContentViewer createViewer(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI);
		
		// Multi-font support only works in JFace 3.5 and above (specifically, 3.5 M4 and above).
		// With JFace 3.4, the font information (bold in this example) will be ignored.
//		FontData[] boldFontData= getModifiedFontData(viewer.getTree().getFont().getFontData(), SWT.BOLD);
//		Font boldFont = new Font(Display.getCurrent(), boldFontData);

		final SpaceTreeLabelProvider labelProvider = new SpaceTreeLabelProvider();
		// Work around for bug #19
		if(isOSX()) {
			viewer.getTree().setHeaderVisible(true);
		}
		contentProvider = new SpaceTreeContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setInput(space);
		viewer.setComparator(new TreeViewerComparator());

		/* implement the mouse tracker the action hover handlers*/
		final MouseTracker mouseTracker = new MouseTracker(viewer.getTree()) {
			private PopupDialog informationControl;

			@Override
			protected Object getItemAt(Point point) {
				TreeItem treeItem = viewer.getTree().getItem(point);
				if (treeItem == null)
					return null;
				
				IShadowEntity targetEntity = (IShadowEntity)treeItem.getData();
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				List<IShadowEntity> selectionList = selection.toList();
				
				if (selectionList.contains(targetEntity) && selectionList.size()>1) {
					FolderEntity folder = new FolderEntity(targetEntity.getRealmId(), null, "Selection");
					IShadowEntity folderShadow = TreeStructureContext.createRoot(folder);
					for (IShadowEntity entity: selectionList)
						((TreeStructureContext)folderShadow.getStructureContext()).addChild(entity);
					return folderShadow;
				}
				
				return targetEntity;
			}

			@Override
			protected Rectangle getAreaOfItemAt(Point point) {
				TreeItem treeItem = viewer.getTree().getItem(point);
				if (treeItem != null) {
					Rectangle treeItemArea = treeItem.getBounds();

					/*
					 * the TreeItem getBounds rectangle only includes the text. But
					 * getItem(point) returns the rectangle for any point in the
					 * tree row.
					 */
					return expandedItemArea(treeItemArea);
				}
				return super.getAreaOfItemAt(point);
			}
			
			@Override
			protected Rectangle getAreaOfSelectedItem() {
				TreeItem[] selection = viewer.getTree().getSelection();
				if(selection != null && selection.length > 0) {
					return selection[0].getBounds();
				}
				return null;
			}
			
			private Rectangle expandedItemArea(Rectangle itemArea) {
				return new Rectangle(Math.max(itemArea.x - 12, 2), Math.max(itemArea.y
						- EPSILON * 2, 0), itemArea.width + 12 * 2, itemArea.height
						+ EPSILON * 2 * 2);
			}
			
			@Override
			protected void showInformationControl(Shell parent, Point location,
					Object input, Object item) {
				informationControl = new ActionHover(parent, location, input, item);
				informationControl.open();
			}
			@Override
			protected void hideInformationControl() {
				if(informationControl != null) {
					informationControl.close();
				}
			}
			@Override
			protected boolean focusInformationControl() {
				if(informationControl != null) {
					Shell shell = informationControl.getShell();
					if(shell != null) {
						return shell.setFocus();
					}
				}
				return false;
			}
			@Override
			protected Rectangle getInformationControlArea() {
				if(informationControl != null) {
					Shell shell = informationControl.getShell();
					if(shell != null) {
						return shell.getBounds();
					}
				}
				return null;
			}
		};
		
		viewer.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				mouseTracker.stop();
			}
		});

		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {EntityTransfer.getInstance() /*, TextTransfer.getInstance()*/}, new ViewerDropAdapter(viewer) {
			@Override
			public boolean performDrop(Object data) {
				System.out.println("drop "+data);
				if (data instanceof IEntity[]) {
					for (IEntity entity: (IEntity[])data) {
						space.addEntity(entity);
					}
					return true;
				} else if (data instanceof String) {
					IInputBarActionProviderService actionProvider = Activator.getDefault().getInputBarActionProvider();
					for (String line: ((String) data).split("[\\r\\n]+")) {
						List<IAction> actions = actionProvider.getActions(space.getProbeId(), space.getId(), line);
						if (actions.size() > 0) {
							IAction action = actions.get(0);
							System.out.println("run "+action);
							if (action instanceof ISpaceAction)
								((ISpaceAction) action).setSpace(space);
							try {
								action.run();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					return true;
				}
				return false;
			}

			@Override
			public boolean validateDrop(Object target, int op, TransferData type) {
				setFeedbackEnabled(false);
				setExpandEnabled(false);
				setSelectionFeedbackEnabled(false);
				return EntityTransfer.getInstance().isSupportedType(type) || TextTransfer.getInstance().isSupportedType(type);
			}
		});
		
		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] {TextTransfer.getInstance(), EntityTransfer.getInstance()}, new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				if (EntityTransfer.getInstance().isSupportedType(event.dataType)) {
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					List<IEntity> entities = new ArrayList<IEntity>();
					Iterator<?> iterator = selection.iterator();
					while (iterator.hasNext()) {
						Object o = iterator.next();
						if (o instanceof IShadowEntity) {
							IEntity e = ((IShadowEntity)o).getRealEntity();
//							if (!entities.contains(e))
								entities.add(e);
						}
					}

					event.data = entities.toArray(new IEntity[entities.size()]);
				} else if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					StringBuffer buffer = new StringBuffer();
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					Iterator<?> iterator = selection.iterator();
					while (iterator.hasNext()) {
						buffer.append(labelProvider.getText(iterator.next()));
						buffer.append("\n");
					}
					event.data = buffer.toString();
				}
			}
		});
		
		return viewer;
	}

/*	private static FontData[] getModifiedFontData(FontData[] originalData, int additionalStyle) {
		FontData[] styleData = new FontData[originalData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = originalData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | additionalStyle);
		}
       	return styleData;
    }
*/
	private boolean isOSX() {
		final String os =  System.getProperty("osgi.os");
		return (os != null && os.equals("macosx"));
	}

	public void focusEntity(IEntity entity) {
		TreeBuilder tb = contentProvider.getTreeBuilder();
		if (entity instanceof FolderEntity)
			return;
		List<IShadowEntity> shadows = tb.getShadows(entity.getId());
		IShadowEntity lastShadow = null;
		for(IShadowEntity s : shadows) {
			viewer.reveal(s);
			viewer.expandToLevel(s, 10);
			lastShadow = s;
		}
		if (lastShadow != null) {
			viewer.setSelection(new StructuredSelection(lastShadow), true);
		} else {
			viewer.setSelection(new StructuredSelection(), false);
		}
	}
}
