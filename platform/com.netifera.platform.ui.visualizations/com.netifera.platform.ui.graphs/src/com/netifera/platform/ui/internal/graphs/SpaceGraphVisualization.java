package com.netifera.platform.ui.internal.graphs;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import prefuse.controls.ControlAdapter;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.layers.IEdgeLayerProvider;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.api.model.layers.ILayerProvider;
import com.netifera.platform.ui.graphs.GraphViewer;
import com.netifera.platform.ui.graphs.actions.ToggleAnimationAction;
import com.netifera.platform.ui.graphs.actions.ToggleEnforceBoundsAction;
import com.netifera.platform.ui.graphs.actions.ToggleImagesAction;
import com.netifera.platform.ui.graphs.actions.ToggleLabelsAction;
import com.netifera.platform.ui.graphs.actions.ToggleOverviewAction;
import com.netifera.platform.ui.spaces.actions.ActionHover;
import com.netifera.platform.ui.spaces.actions.ChooseLayerAction;
import com.netifera.platform.ui.spaces.actions.SelectLayersAction;
import com.netifera.platform.ui.spaces.graphs.SpaceGraphContentProvider;
import com.netifera.platform.ui.spaces.tree.SpaceTreeLabelProvider;
import com.netifera.platform.ui.spaces.visualizations.ISpaceVisualization;

public class SpaceGraphVisualization implements ISpaceVisualization {

	final private ISpace space;
	private GraphViewer viewer;
	private SpaceGraphContentProvider contentProvider;
	
	public SpaceGraphVisualization(ISpace space) {
		this.space = space;
	}
	
	public ContentViewer createViewer(final Composite parent) {
		viewer = new GraphViewer(parent);
		contentProvider = new SpaceGraphContentProvider();
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new SpaceTreeLabelProvider());
		viewer.setInput(space);

		/* implement the mouse tracker the action hover handlers*/
		viewer.getControl().addPrefuseControlListener(new ControlAdapter() {
			private int EPSILON = 3;
			private PopupDialog informationControl;
			private VisualItem activeItem;

			@Override
		    public void mousePressed(MouseEvent e) {
				if (informationControl != null)
		        	parent.getDisplay().syncExec(new Runnable() {
		        		public void run() {
		        			hideInformationControl();
		        		}
		        	});
			}
			
			@Override
		    public void itemPressed(VisualItem item, MouseEvent e) {
				mousePressed(e);
			}

		    /**
		     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
		     */
			@Override
		    public void itemEntered(VisualItem item, final MouseEvent e) {
		        activeItem = item;
		        if (item instanceof NodeItem)
		        	parent.getDisplay().syncExec(new Runnable() {
		        		public void run() {
		        			Point p = viewer.getControl().toDisplay(e.getX(), e.getY());
		        			showInformationControl(parent.getShell(), new Point(p.x+EPSILON,p.y+EPSILON), space, activeItem.get("entity"));
		        		}
		        	});
		    }
		    
		    /**
		     * @see prefuse.controls.Control#mouseMoved(java.awt.event.MouseEvent)
		     */
			@Override
			public void mouseMoved(final MouseEvent e) {
	        	parent.getDisplay().syncExec(new Runnable() {
	        		public void run() {
	    				Rectangle areaOfItem = getAreaOfItem();
	    				Rectangle informationControlArea = getInformationControlArea();
	    				if (areaOfItem == null || informationControlArea == null)
	    					return;
	        			Point p = viewer.getControl().toDisplay(e.getX(), e.getY());
	    				if (!areaOfItem.contains(p.x,p.y) && !informationControlArea.contains(p.x,p.y)) {
	    		        			hideInformationControl();
	    		        		}
	    				}
	        	});
			}

		    /**
		     * @see prefuse.controls.Control#mouseExited(java.awt.event.MouseEvent)
		     */
			@Override
			public void mouseExited(final MouseEvent e) {
				mouseMoved(e);
			}

			protected Rectangle getAreaOfItem() {
				if (activeItem != null) {
					Rectangle2D itemArea = activeItem.getBounds();

					return new Rectangle(Math.max((int)itemArea.getX() - 20, 0), Math.max((int)itemArea.getY() - 20, 0),
							(int)itemArea.getWidth() + 20 * 2, (int)itemArea.getHeight() + 20 * 2);
				}
				return null;
			}
		
			private void showInformationControl(Shell parent, Point location,
					Object input, Object item) {
				hideInformationControl();
				informationControl = new ActionHover(parent, location, input, item);
				informationControl.open();
			}
			
			private void hideInformationControl() {
				if(informationControl != null) {
					informationControl.close();
				}
			}
			
			private Rectangle getInformationControlArea() {
				if(informationControl != null) {
					Shell shell = informationControl.getShell();
					
					if(shell != null) {
						return new Rectangle(Math.max(shell.getBounds().x - 20, 0), Math.max(shell.getBounds().y - 20, 0),
								shell.getBounds().width + 20 * 2, shell.getBounds().height + 20 * 2);
					}
				}
				return null;
			}
		});

		return viewer;
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
				return contentProvider.getLayers();
			}
			@Override
			protected List<ILayerProvider> getLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
					if (layerProvider instanceof IEdgeLayerProvider
						|| layerProvider instanceof IGroupLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
		});

		contributions.add(new ChooseLayerAction("Set Color", Activator.getDefault().getImageCache().getDescriptor("icons/colors.png")) {
			@Override
			protected List<ILayerProvider> getLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
					if (layerProvider instanceof IGroupLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
			@Override
			protected ILayerProvider getActiveLayer() {
				return contentProvider.getColorLayer();
			}
			@Override
			protected void setActiveLayer(ILayerProvider provider) {
				contentProvider.setColorLayer((IGroupLayerProvider) provider);
			}
			
		});

		contributions.add(new ChooseLayerAction("Set Shape", Activator.getDefault().getImageCache().getDescriptor("icons/shape.png")) {
			@Override
			protected List<ILayerProvider> getLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
					if (layerProvider instanceof IGroupLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
			@Override
			protected ILayerProvider getActiveLayer() {
				return contentProvider.getShapeLayer();
			}
			@Override
			protected void setActiveLayer(ILayerProvider provider) {
				contentProvider.setShapeLayer((IGroupLayerProvider) provider);
			}
			
		});

		contributions.add(new ToggleAnimationAction(viewer.getControl()));
		contributions.add(new ToggleOverviewAction(viewer.getControl()));
		contributions.add(new ToggleEnforceBoundsAction(viewer.getControl()));
		contributions.add(new ToggleLabelsAction(viewer.getControl()));
		contributions.add(new ToggleImagesAction(viewer.getControl()));
	}

	public void focusEntity(IEntity entity) {
		// TODO Auto-generated method stub
		
	}
}
