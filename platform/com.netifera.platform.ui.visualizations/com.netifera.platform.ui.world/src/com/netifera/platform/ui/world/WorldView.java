package com.netifera.platform.ui.world;


import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.BMNGOneImage;
import gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.view.FlyToOrbitViewStateIterator;
import gov.nasa.worldwind.view.OrbitView;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.netifera.platform.api.events.IEvent;
import com.netifera.platform.api.events.IEventHandler;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.api.model.IShadowEntity;
import com.netifera.platform.api.model.ISpace;
import com.netifera.platform.api.model.ISpaceContentChangeEvent;
import com.netifera.platform.api.model.layers.IEdge;
import com.netifera.platform.api.model.layers.IEdgeLayerProvider;
import com.netifera.platform.api.model.layers.IGroupLayerProvider;
import com.netifera.platform.api.model.layers.ILayerProvider;
import com.netifera.platform.net.geoip.IGeographicalLayerProvider;
import com.netifera.platform.net.geoip.ILocation;
import com.netifera.platform.ui.api.model.IEdgeWithStyle;
import com.netifera.platform.ui.internal.world.Activator;
import com.netifera.platform.ui.internal.world.ConcurrentRenderableLayer;
import com.netifera.platform.ui.spaces.SpaceEditorInput;
import com.netifera.platform.ui.spaces.actions.ActionHover;
import com.netifera.platform.ui.spaces.actions.ChooseLayerAction;
import com.netifera.platform.ui.spaces.actions.SelectLayersAction;
import com.netifera.platform.ui.world.actions.ToggleFollowNewEntitiesAction;
import com.netifera.platform.ui.world.actions.ToggleLabelsAction;
import com.netifera.platform.ui.world.actions.ToggleOverviewAction;
import com.netifera.platform.ui.world.actions.TogglePlaceNamesAction;

public class WorldView extends ViewPart {

	public static final String ID = "com.netifera.platform.views.world";

	private Composite SWT_AWT_container;
	private Frame awtFrame;
	private LayerList layerList = new LayerList();
	private WorldWindowGLCanvas worldWindow;
	
	private WorldMapLayer overviewLayer;
	private ScalebarLayer scalebarLayer;
	private PlaceNameLayer placeNameLayer;
	
	private AnnotationLayer labelLayer;
	private RenderableLayer polylineLayer;
	
	private AnnotationLayer focusLabelLayer;
	private volatile boolean followNewEntities = true;
	private IEntity focusEntity;

	private List<ILayerProvider> layerProviders = new ArrayList<ILayerProvider>();
	private IGroupLayerProvider colorLayerProvider;

	private ISpace space;
	private IEventHandler spaceChangeListener;
	
	private Map<IEntity,EntityAnnotation> entityToAnnotationMap;

	static private final long FLY_TIME_MSECS = 2000; // time to fly to focused entity
	
	static private final Color[] palette = new Color[] {
		new Color(255, 255, 150, 192),
		new Color(202, 62, 94, 192),
		new Color(255, 152, 213, 192),
		new Color(83, 140, 208, 192),
		new Color(178, 220, 205, 192),
		new Color(146, 248, 70, 192),
		
		new Color(255,255,0, 192),
		new Color(255,200,47, 192),
		new Color(255,118,0, 192),
		new Color(255,0,0, 192),
		new Color(175,13,102, 192),
		new Color(121,33,135, 192)
		
/*		new Color(0xff, 0xc8, 0x00, 0xc0),
		new Color(0xff, 0x00, 0xc8, 0xc0),
		new Color(0xc8, 0x00, 0xff, 0xc0),
		new Color(0x00, 0xc8, 0xff, 0xc0),
		new Color(0xc8, 0xff, 0x00, 0xc0),
		new Color(0x00, 0xff, 0xc8, 0xc0),

/*		Color.RED,
		Color.GREEN,
		Color.BLUE,
		Color.YELLOW,
		Color.MAGENTA,
		Color.ORANGE,
		Color.CYAN,
		Color.PINK
*/	};

	
	public WorldView() {
	  Configuration.setValue(AVKey.OFFLINE_MODE, false);
	  
//		Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
//		Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());

//		this.layerList.add(new StarsLayer());
//		this.layerList.add(new FogLayer());
		
		overviewLayer = new WorldMapLayer();
		overviewLayer.setPosition(WorldMapLayer.NORTHEAST);
		this.layerList.add(overviewLayer);
		
//		this.layerList.add(new WorldBordersFreemapLayer());
//		this.layerList.add(new WorldBordersKatrinaOWSLayer());
//		this.layerList.add(new CountryBoundariesLayer());
//		this.layerList.add(new TerrainProfileLayer());
		
		this.layerList.add(new BMNGOneImage());
//		this.layerList.add(new BMNGSurfaceLayer());
//		this.layerList.add(new OpenStreetMapLayer());



		scalebarLayer = new ScalebarLayer();
		this.layerList.add(scalebarLayer);

//		this.layerList.add(new CompassLayer());
//		this.layerList.add(new SkyColorLayer());
//		this.layerList.add(new FogLayer());
		
		SkyGradientLayer atmosphere = new SkyGradientLayer();
		atmosphere.setAtmosphereThickness(100e3);
		atmosphere.setHorizonColor(new Color(0.66f/1.3f, 0.70f/1.3f, 0.81f/1.3f));
		atmosphere.setZenithColor(new Color(0.26f, 0.47f, 0.83f));
		this.layerList.add(atmosphere);
		
		labelLayer = new AnnotationLayer();
		labelLayer.setEnabled(false);
		this.layerList.add(labelLayer);

		focusLabelLayer = new AnnotationLayer();
		focusLabelLayer.setEnabled(false);
		this.layerList.add(focusLabelLayer);
/*
		RenderableLayer nightLightsLayer = new RenderableLayer();
		String EARTH_NIGHT_LIGHTS_URL = "http://veimages.gsfc.nasa.gov/1438/earth_lights_lrg.jpg"; //$NON-NLS-1$
		SurfaceImage si = new SurfaceImage(EARTH_NIGHT_LIGHTS_URL, Sector.FULL_SPHERE);
		nightLightsLayer.setName("Night Lights");
		nightLightsLayer.addRenderable(si);
		nightLightsLayer.setPickEnabled(false);
		nightLightsLayer.setOpacity(1);
		this.layerList.add(nightLightsLayer);
*/
		
		placeNameLayer = new EarthNASAPlaceNameLayer();
		placeNameLayer.setEnabled(true);
		placeNameLayer.setOpacity(100);
		this.layerList.add(placeNameLayer);
		
		polylineLayer = new ConcurrentRenderableLayer();
		this.layerList.add(polylineLayer);
		
		for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
			if (layerProvider.isDefaultEnabled() &&
					(layerProvider instanceof IGeographicalLayerProvider || layerProvider instanceof IEdgeLayerProvider))
				layerProviders.add(layerProvider);
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		this.SWT_AWT_container = new Composite(parent, SWT.NO_BACKGROUND | SWT.BORDER | SWT.EMBEDDED);
		this.SWT_AWT_container.setLayout(new FillLayout());
		this.awtFrame = SWT_AWT.new_Frame(this.SWT_AWT_container);
		this.worldWindow = new WorldWindowGLCanvas();
		this.awtFrame.add(this.worldWindow);
		Model model = (Model) WorldWind
				.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		model.setLayers(this.layerList);
		model.setShowWireframeExterior(false);
		model.setShowWireframeInterior(false);
		model.setShowTessellationBoundingVolumes(false);
		this.worldWindow.setModel(model);
		
		this.worldWindow.addSelectListener(new SelectListener() {
			private int EPSILON = 3;
			volatile private PopupDialog informationControl;
			volatile private EntityAnnotation activeItem;
			
			public void selected(final SelectEvent e) {
				System.out.println("selected "+e);
				System.out.println("top object: "+e.getTopObject());
				Object o = e.getTopObject();
				if (e.getEventAction().equals(SelectEvent.ROLLOVER)) {
					if (o instanceof EntityPolyline) {
						final String label = Activator.getDefault().getLabelProvider().getFullText((IShadowEntity)((EntityPolyline)o).getEntity());
						if (label != null) {
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									WorldView.this.getViewSite().getActionBars().getStatusLineManager().setMessage(label);
								}
							});
						}
					}
				}
				
				if (!e.getEventAction().equals(SelectEvent.HOVER) && !e.getEventAction().equals(SelectEvent.ROLLOVER))
					return;

				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						Point location = e.getMouseEvent() != null ? new Point(e.getMouseEvent().getX()/*.getXOnScreen()*/+EPSILON,e.getMouseEvent().getY()/*.getYOnScreen()*/+EPSILON) : Display.getDefault().getCursorLocation();
						Object o = e.getTopObject();
						if (o == null) {
							return;
						}
						if (activeItem != o) {
//							Rectangle areaOfItem = getAreaOfItem();
							Rectangle informationControlArea = getInformationControlArea();
							if (informationControlArea != null && !informationControlArea.contains(location)) {
								hideInformationControl();
							}
						}
						if (e.getEventAction().equals(SelectEvent.HOVER)) {
							if (o instanceof EntityAnnotation) {
								activeItem = (EntityAnnotation) o;
								showInformationControl(parent.getShell(), location, space, activeItem.getEntity());
							}
						}
					}
				});
			}

			private void showInformationControl(Shell parent, Point location,
					Object input, Object item) {
				hideInformationControl();
				informationControl = new ActionHover(parent, location, input, item);
				informationControl.open();
				System.out.println("show");
			}
			
			private void hideInformationControl() {
				if(informationControl != null) {
					System.out.println("hide");
					informationControl.close();
					informationControl = null;
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
		
		IPageListener pageListener = new IPageListener() {
			IPartListener partListener = new IPartListener() {
				public void partActivated(IWorkbenchPart part) {
					if (!(part instanceof IEditorPart))
						return;
					IEditorInput editorInput = ((IEditorPart) part).getEditorInput();
					if (editorInput instanceof SpaceEditorInput) {
						ISpace newSpace = ((SpaceEditorInput)editorInput).getSpace();
						setPartName(newSpace.getName());//FIXME this is because the name changes and we dont get notified
						if (newSpace != WorldView.this.space)
							setSpace(newSpace);
					}
				}
				public void partBroughtToTop(IWorkbenchPart part) {
				}
				public void partClosed(IWorkbenchPart part) {
				}
				public void partDeactivated(IWorkbenchPart part) {
				}
				public void partOpened(IWorkbenchPart part) {
				}
			};
			public void pageActivated(IWorkbenchPage page) {
				page.addPartListener(partListener);
				IEditorPart editor = page.getActiveEditor();
				if (editor != null) partListener.partActivated(editor);
			}
			public void pageClosed(IWorkbenchPage page) {
				page.removePartListener(partListener);
			}
			public void pageOpened(IWorkbenchPage page) {
			}
		};
		
		getSite().getWorkbenchWindow().addPageListener(pageListener);
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page != null)
			pageListener.pageActivated(page);

		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(
				//"com.netifera.platform.editors.spaces",
				new ISelectionListener() {
					public void selectionChanged(IWorkbenchPart part, org.eclipse.jface.viewers.ISelection sel) {
						if(sel instanceof IStructuredSelection && !sel.isEmpty()) {
							Object o = ((IStructuredSelection)sel).iterator().next();
							if(o instanceof IEntity) {
								focusEntity((IEntity)o);
							}
						}
					}
					
				});
/*		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page == null)
			return;
*/	
	
		initializeToolBar();
	}

	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolbarManager.add(new SelectLayersAction() {
			@Override
			protected void disableLayer(ILayerProvider provider) {
				removeLayer(provider);
			}
			@Override
			protected void enableLayer(ILayerProvider provider) {
				addLayer(provider);
			}
			@Override
			protected List<ILayerProvider> getActiveLayers() {
				return WorldView.this.getLayers();
			}
			@Override
			protected List<ILayerProvider> getLayers() {
				List<ILayerProvider> answer = new ArrayList<ILayerProvider>();
				for (ILayerProvider layerProvider: Activator.getDefault().getModel().getLayerProviders()) {
					if (layerProvider instanceof IGeographicalLayerProvider || layerProvider instanceof IEdgeLayerProvider)
						answer.add(layerProvider);
				}
				return answer;
			}
		});
		
		toolbarManager.add(new ChooseLayerAction("Set Color", Activator.getDefault().getImageCache().getDescriptor("icons/colors.png")) {
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
				return getColorLayer();
			}
			@Override
			protected void setActiveLayer(ILayerProvider provider) {
				setColorLayer((IGroupLayerProvider)provider);
			}
		});
		
		toolbarManager.add(new ToggleOverviewAction(this));
		toolbarManager.add(new ToggleLabelsAction(this));
		toolbarManager.add(new TogglePlaceNamesAction(this));
		toolbarManager.add(new ToggleFollowNewEntitiesAction(this));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
	
	private void setSpace(ISpace space) {
		if (this.space != null)
			this.space.removeChangeListener(spaceChangeListener);
	
		labelLayer.removeAllAnnotations();
		polylineLayer.removeAllRenderables();
		
		focusLabelLayer.removeAllAnnotations();
		
		entityToAnnotationMap = new HashMap<IEntity,EntityAnnotation>();
		
		this.space = space;
		spaceChangeListener = new IEventHandler() {
			public void handleEvent(IEvent event) {
				if(event instanceof ISpaceContentChangeEvent) {
					handleSpaceChange((ISpaceContentChangeEvent)event);
				}
			}
		};
		if (space != null) {
			space.addChangeListenerAndPopulate(spaceChangeListener);
			setPartName(space.getName());
		} else {
			setPartName("WorldView");
		}
		worldWindow.repaint();
	}
	
	private void handleSpaceChange(final ISpaceContentChangeEvent event) {
		if (!(event.getEntity() instanceof AbstractEntity))
			return;
		final AbstractEntity entity = (AbstractEntity)event.getEntity();
		if(event.isCreationEvent()) {
			addEntity(entity);
		} else if(event.isUpdateEvent()) {
			updateEntity(entity);
		} else if(event.isRemovalEvent()) {
			removeEntity(entity);
		}
	}

	private synchronized void addEntity(IEntity entity) {
		if (entity.getTypeName().equals("host")) {
			addNode(entity);
			if (followNewEntities) {
				focusEntity(entity);
			}
		}
		
		for (ILayerProvider layerProvider: layerProviders) {
			if (layerProvider instanceof IEdgeLayerProvider) {
				IEdgeLayerProvider edgeLayerProvider = (IEdgeLayerProvider)layerProvider;
				for (IEdge edge: edgeLayerProvider.getEdges(entity)) {
					addEdge(edge);
				}
			}
		}
		
		worldWindow.repaint();//XXX is this needed here?
	}

	private synchronized void updateEntity(IEntity entity) {
		// TODO
		for (ILayerProvider layerProvider: layerProviders) {
			if (layerProvider instanceof IEdgeLayerProvider) {
				IEdgeLayerProvider edgeLayerProvider = (IEdgeLayerProvider)layerProvider;
				for (IEdge edge: edgeLayerProvider.getEdges(entity)) {
					addEdge(edge);
				}
			}
		}

		if (entity == focusEntity)
			focusEntity(entity); // refocus, update label
		
		worldWindow.repaint();//XXX is this needed here?
	}
	
	private synchronized void removeEntity(IEntity entity) {
		EntityAnnotation annotation = entityToAnnotationMap.remove(entity);
		if (annotation != null) {
			labelLayer.removeAnnotation(annotation);
			//TODO remove polylines, etc
			worldWindow.repaint();
		}
	}

	private ILocation getLocation(IEntity entity) {
		for (ILayerProvider layerProvider: layerProviders) {
			if (layerProvider instanceof IGeographicalLayerProvider) {
				ILocation location = ((IGeographicalLayerProvider)layerProvider).getLocation(entity);
				if (location != null) {
					return location;
				}
			}
		}
		
		//XXX for testing
/*		if (entity.getTypeName().equals("host")) {
			return new ILocation() {
				public String getCity() {
					return "Beijing";
				}
				public String getCountry() {
					return "China";
				}
				public String getCountryCode() {
					return "CN";
				}
				public double[] getPosition() {
					return new double[] {35.0,105.0,0.0};
				}
				public String getPostalCode() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
*/		
		return null;
	}

	private Position getPosition(IEntity entity) {
		ILocation location = getLocation(entity);
		if (location != null) {
			double[] pos = location.getPosition();
			return Position.fromDegrees(pos[0],pos[1],0);
		}
		
		return null;
	}

	static final private Random random = new Random();
	private void addVerticalLine(IEntity entity, Position position, Color color) {
		final List<Polyline> polylines = new ArrayList<Polyline>();

		double scale = 1.0 + 0.1*(0.5 - random.nextGaussian());
		double length = 2500000.0 * scale / 10;
		double dx = random.nextGaussian()*0.2/10;
		double dy = random.nextGaussian()*0.2/10;
//		int alpha[] = {1, 2, 3, 5, 10, 20, 40, 80, 160, 255};
//		int alpha[] = {1, 2, 3, 5, 10, 20, 30, 40, 60, 160};
		int alphas[] = {10, 15, 25, 40, 60, 80, 100, 120, 140, 160};
//		int white[] = {255, 220, 200, 150, 100, 50, 20, 10, 0, 0};
		int white[] = {0,0,0,0,0,0,0,0,0,0,0};
		for (int i=10; i>=1; i--) {
			int alpha = alphas[i-1] * color.getAlpha() / 255;
			List<Position> positions = new ArrayList<Position>();
			positions.add(position);
			position = Position.fromDegrees(position.getLatitude().getDegrees()+dx, position.getLongitude().getDegrees()+dy, position.getElevation()+length);
			positions.add(position);
			final Polyline polyline = new EntityPolyline(entity, positions);
			polyline.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
			polyline.setColor(new Color(Math.max(color.getRed(),white[i-1]), Math.max(color.getGreen(),white[i-1]), Math.max(color.getBlue(),white[i-1]), alpha));
			polyline.setLineWidth(2);
			polylines.add(polyline);
		}
		
		for (Polyline polyline: polylines)
			polylineLayer.addRenderable(polyline);
	}

	private Color getColor(String group) {
		int v = group.hashCode();
		if (v < 0) v = -v;
		return palette[v % palette.length];
	}
	
	private Color getColor(IEntity entity) {
		if (colorLayerProvider != null) {
			for (String group: colorLayerProvider.getGroups(entity)) {
				return getColor(group);
			}
			return new Color(0xff, 0xff, 0xff, 0x90);
		}
		return new Color(0xc8, 0xc8, 0xc8, 0x90);	
	}
	
	private Position addNode(IEntity entity) {
		EntityAnnotation annotation = entityToAnnotationMap.get(entity);
		if (annotation != null) //TODO should update if position changed
			return annotation.getPosition();
		Position position = getPosition(entity);
		if (position == null)
			return null;

		addVerticalLine(entity, position, getColor(entity));
		
		String text = Activator.getDefault().getLabelProvider().getText((IShadowEntity)entity);
		if (text == null) text = entity.toString();
		annotation = new EntityAnnotation(entity, text, position, true);
		entityToAnnotationMap.put(entity, annotation);
		final EntityAnnotation annotationFinal = annotation;
		labelLayer.addAnnotation(annotationFinal);
		return position;
	}
	
	private void addEdge(IEdge edge) {
		Position sourcePosition = addNode(edge.getSource());
		Position targetPosition = addNode(edge.getTarget());
		if (sourcePosition == null || targetPosition == null)
			return;
		
		List<Position> positions = new ArrayList<Position>();
		positions.add(sourcePosition);
		positions.add(targetPosition);
		final Polyline polyline = new Polyline(positions);
		polyline.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
		if (edge instanceof IEdgeWithStyle) {
			IEdgeWithStyle edgeWithStyle = (IEdgeWithStyle) edge;
			polyline.setColor(edgeWithStyle.getColor());
			polyline.setLineWidth(edgeWithStyle.getLineWidth());
			if (edgeWithStyle.getLineStyle() == IEdgeWithStyle.STYLE_DASHED) {
				polyline.setStipplePattern((short)0x5555);
				polyline.setStippleFactor(7);
			}
		} else {
			polyline.setColor(Color.WHITE);
			polyline.setLineWidth(1);
			polyline.setStipplePattern((short)0x5555);
			polyline.setStippleFactor(7);
		}
//		polyline.setHighlighted(true);
		polyline.setFollowTerrain(true);
//		polyline.setPathType(Polyline.GREAT_CIRCLE);
		polyline.setPathType(Polyline.RHUMB_LINE);
		polylineLayer.addRenderable(polyline);
	}
	
	public boolean isPlaceNamesEnabled() {
		return placeNameLayer.isEnabled();
	}
	
	public void setPlaceNamesEnabled(boolean enabled) {
		placeNameLayer.setEnabled(enabled);
		worldWindow.repaint();
	}
	
	public boolean isOverviewEnabled() {
		return overviewLayer.isEnabled();
	}
	
	public void setOverviewEnabled(boolean enabled) {
		overviewLayer.setEnabled(enabled);
		scalebarLayer.setEnabled(enabled);
		worldWindow.repaint();
	}
	
	public boolean isLabelsEnabled() {
		return labelLayer.isEnabled();
	}
	
	public void setLabelsEnabled(boolean enabled) {
		labelLayer.setEnabled(enabled);
		worldWindow.repaint();
	}
	
	public void addLayer(ILayerProvider layerProvider) {
		layerProviders.add(layerProvider);
		setSpace(space);//to repopulate
	}
	
	public void removeLayer(ILayerProvider layerProvider) {
		layerProviders.remove(layerProvider);
		setSpace(space);//to repopulate
	}

	public List<ILayerProvider> getLayers() {
		return layerProviders;
	}
	
	public IGroupLayerProvider getColorLayer() {
		return colorLayerProvider;
	}
	
	public void setColorLayer(IGroupLayerProvider layerProvider) {
		colorLayerProvider = layerProvider;
		setSpace(space);//to repopulate
	}

	public synchronized void setFollowNewEnabled(boolean enabled) {
		followNewEntities = enabled;
	}

	public synchronized boolean isFollowNewEnabled() {
		return followNewEntities;
	}

	public synchronized void focusEntity(IEntity entity) {
		if (entity == null) {
			focusLabelLayer.setEnabled(false);
			focusEntity = null;
		} else {
			focusLabelLayer.removeAllAnnotations();
			
			Position position = getPosition(entity);
			if (position == null)
				return;
			
			String text = Activator.getDefault().getLabelProvider().getText((IShadowEntity)entity);
			if (text == null) text = entity.toString();
			ILocation location = getLocation(entity);
			if (location != null) {
				String locationText = "";
				if (location.getCity() != null) {
					locationText = location.getCity()+", "+location.getCountry();
				} else if (location.getCountry() != null) {
					locationText = location.getCountry();
				} else {
					locationText = location.getPosition()[0]+" "+location.getPosition()[1];
				}
				text += "<br>"+locationText;
			}
			
			EntityAnnotation annotation = new EntityAnnotation(entity, text, position, false);
			focusLabelLayer.addAnnotation(annotation);
			
/*			GlobeAnnotation mark = new GlobeAnnotation("Annotation with extra frames drawn by a render delegate.", Position.fromDegrees(40, -116, 0), Font.decode("Serif-BOLD-18"), Color.DARK_GRAY)
			{
			};
			mark.getAttributes().setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
			mark.getAttributes().setBackgroundColor(new Color(1f, 1f, 1f, .7f));
			mark.getAttributes().setBorderColor(Color.BLACK);
			mark.getAttributes().setSize(new Dimension(160, 200));
			focusLabelLayer.addAnnotation(mark);
*/			
			focusLabelLayer.setEnabled(true);

			if (focusEntity == entity) // dont move again to the entity position, just update label
				return;
			
			focusEntity = entity;
			
			// fly to position:
			
			OrbitView orbitView = (OrbitView) worldWindow.getView();
						
			Position beginCenter = orbitView.getCenterPosition();
			Angle beginHeading = orbitView.getHeading();
			Angle beginPitch = orbitView.getPitch();
			double beginZoom = orbitView.getZoom();

			Position endCenter = new Position(position.getLatLon(), orbitView.getEyePosition().getElevation());
//			Angle endHeading = Angle.fromDegrees(Configuration.getDoubleValue(AVKey.INITIAL_HEADING));
//			Angle endPitch = Angle.fromDegrees(Configuration.getDoubleValue(AVKey.INITIAL_PITCH));
			double endZoom = beginZoom;

			endZoom = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE) / 2.5;

			ViewStateIterator vsi = FlyToOrbitViewStateIterator.createPanToIterator(
					worldWindow.getModel().getGlobe(),
					beginCenter, endCenter,
					beginHeading, beginHeading,
					beginPitch, beginPitch,
					beginZoom, endZoom,
					FLY_TIME_MSECS,
					true);
			
			orbitView.applyStateIterator(vsi);
		}
	}
}
