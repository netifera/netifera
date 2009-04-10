package com.netifera.platform.ui.graphs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TableListener;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.ui.graphs.utils.AWTEmbeddedControl;
import com.netifera.platform.ui.graphs.utils.ImageConverter;

public class GraphControl extends AWTEmbeddedControl {
	private static final String GRAPH = "graph";
	private static final String NODES = "graph.nodes";
	private static final String EDGES = "graph.edges";
	private static final String AGGREGATES = "aggregates";

	private Graph graph = null;
	private Visualization visualization;
	private Display overview;
	private AggregateTable aggregateTable;
	private ForceDirectedLayout forceLayout;

	private boolean showLabels;
	private boolean showImages;
	private boolean enforceBounds;
	
	private ColumnLabelProvider nodeLabelProvider;
	private Map<String, AggregateItem> aggregates;

	static private int[] palette = new int[] {
		ColorLib.rgb(255, 255, 150),
		ColorLib.rgb(205, 145, 63),
		ColorLib.rgb(202, 62, 94),
		ColorLib.rgb(255, 152, 213),
		ColorLib.rgb(83, 140, 208),
		ColorLib.rgb(178, 220, 205),
		ColorLib.rgb(146, 248, 70),
		ColorLib.rgb(175, 200, 74)
		
/*			ColorLib.rgb(0xff, 0xc8, 0xc8),
			ColorLib.rgb(0xc8, 0xff, 0xc8),
			ColorLib.rgb(0xc8, 0xc8, 0xff),
			ColorLib.rgb(0xc8, 0xff, 0xff),
			ColorLib.rgb(0xff, 0xc8, 0xff),
			ColorLib.rgb(0xff, 0xff, 0xc8),
*/			};

    static private int[] aggregatesPalette = new int[] {
			ColorLib.rgba(0xff, 0xc8, 0xc8, 150),
			ColorLib.rgba(0xc8, 0xff, 0xc8, 150),
			ColorLib.rgba(0xc8, 0xc8, 0xff, 150),
			ColorLib.rgba(0xc8, 0xff, 0xff, 150),
			ColorLib.rgba(0xff, 0xc8, 0xff, 150),
			ColorLib.rgba(0xff, 0xff, 0xc8, 150),
			};

    
	public GraphControl(Composite parent) {
		super(parent);
		setOverviewEnabled(true);
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
		updateVisualization();
	}

	protected Component createAWTComponent() {
		// create a new, empty visualization for our data
		visualization = new Visualization();
		
		// adds graph to visualization and sets renderer label field
		updateVisualization();

		// fix selected focus nodes
		TupleSet focusGroup = visualization.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i) {
					((VisualItem) rem[i]).setFixed(false);
				}
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(true);
				}
				if (ts.getTupleCount() == 0) { // why?
					ts.addTuple(rem[0]);
					((VisualItem) rem[0]).setFixed(false);
				}
				visualization.run("draw");
			}
		});

		setRenderers();
		setLayoutActions();
		setDrawActions();

		// --------------------------------------------------------------------
		// set up a display to show the visualization

		Display display = new Display(visualization);
		display.setSize(700, 700);
		display.pan(350, 350);
		display.setHighQuality(false);

		// main display controls
		display.addControlListener(new FocusControl());
		display.addControlListener(new AggregateDragControl());
//		display.addControlListener(new DragControl(true, true));
		display.addControlListener(new PanControl(Control.LEFT_MOUSE_BUTTON, false));
		display.addControlListener(new ZoomControl(Control.MIDDLE_MOUSE_BUTTON) {
		    protected int zoom(Display display, Point2D p, double zoom, boolean abs) {
		    	// reverse the zoom to match wwj: up = zoom in, down = zoom out
		    	zoom = 1.0 / zoom;
		    	return super.zoom(display, p, zoom, abs);
		    }
		});
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl(Control.MIDDLE_MOUSE_BUTTON));
		display.addControlListener(new NeighborHighlightControl());
		
		display.setForeground(Color.DARK_GRAY);
		display.setBackground(Color.WHITE);
		
		// launch the visualization
		// now we run our action list
		visualization.runAfter("draw", "layout");
		visualization.run("draw");
	
		return display;
	}

	private void setRenderers() {
		AbstractShapeRenderer nodeRenderer;
		if (showLabels || showImages) {
			nodeRenderer = new LabelRenderer() {
				public String getText(VisualItem item) {
					if (showLabels && item.canGet("entity", IEntity.class))
						return nodeLabelProvider.getText(item.get("entity"));
					return null;
				}
				public Image getImage(VisualItem item) {
					if (showImages && item.canGet("entity", IEntity.class))
						return ImageConverter.convert(nodeLabelProvider.getImage(item.get("entity")));
					return null;
				}
			};
		} else {
			nodeRenderer = new ShapeRenderer();
		}
		
/*		EdgeRenderer edgeRenderer = new EdgeRenderer();
		edgeRenderer.setEdgeType(Constants.EDGE_TYPE_LINE);
		edgeRenderer.setArrowType(Constants.EDGE_ARROW_FORWARD);
		edgeRenderer.setArrowHeadSize(8, 8);
*/		
		DefaultRendererFactory rendererFactory = new DefaultRendererFactory();
		rendererFactory.setDefaultRenderer(nodeRenderer);
//		rendererFactory.setDefaultEdgeRenderer(edgeRenderer);

		// draw aggregates as polygons with curved edges
		PolygonRenderer polygonRenderer = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
		polygonRenderer.setCurveSlack(0.15f);
		rendererFactory.add("ingroup('"+AGGREGATES+"')", polygonRenderer);
		
		visualization.setRendererFactory(rendererFactory);
	}
	
	private void setDrawActions() {
		ActionList draw = new ActionList();
		
		ShapeAction shape = new DataShapeAction(NODES, "shape");
		draw.add(shape);

		ColorAction fill = new DataColorAction(NODES, "color", Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		//		ColorAction fill = new ColorAction(NODES, VisualItem.FILLCOLOR, ColorLib.rgba(0.66f, 0.70f, 0.81f, 1.0f));
		fill.add(VisualItem.FIXED, ColorLib.rgb(255, 100, 100));//, 128));
		fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));//, 128));
		draw.add(fill);

		// comment out to remove aggregates fill color
//		draw.add(new DataColorAction(AGGREGATES, "id", Constants.NOMINAL, VisualItem.FILLCOLOR, aggregatesPalette));
		
		ColorAction aStroke = new ColorAction(AGGREGATES, VisualItem.STROKECOLOR);
		aStroke.setDefaultColor(ColorLib.gray(200));
		aStroke.add("_hover", ColorLib.rgb(255, 100, 100));
		draw.add(aStroke);
		
		draw.add(new ColorAction(NODES, VisualItem.STROKECOLOR, ColorLib.gray(100)));
		draw.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, ColorLib.rgb(0, 0, 0)));
		
		draw.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, ColorLib.gray(128)));
		draw.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, ColorLib.gray(128)));

		visualization.putAction("draw", draw);
	}
	
	private void setLayoutActions() {
		ActionList layout = new ActionList(Activity.INFINITY);

//		ForceDirectedLayout forceLayout = new ForceDirectedLayout(GRAPH, enforceBounds);
/*		{
			protected float getMassValue(VisualItem item) {
				return 1.0f;
//				return item.isHover() ? 2.0f - (item.getColumnCount() * 0.05f); //: 0.5f;
			}
		};
*/
		
		ForceSimulator forceSimulator = new ForceSimulator();
		//forceLayout.getForceSimulator().getForces()[0].setParameter(0, -1.2f);
        forceSimulator.addForce(new NBodyForce(NBodyForce.DEFAULT_GRAV_CONSTANT, 200, NBodyForce.DEFAULT_THETA));
        forceSimulator.addForce(new SpringForce());
        forceSimulator.addForce(new DragForce());
        forceLayout = new ForceDirectedLayout(GRAPH, forceSimulator, enforceBounds);
		layout.add(forceLayout);
		
//		layout.add(new GraphDistanceFilter(GRAPH, 3));
		
		layout.add(new AggregateLayout(AGGREGATES));

		layout.add(new RepaintAction());
		visualization.removeAction("layout");
		visualization.putAction("layout", layout);
		
		visualization.runAfter("draw", "layout");
		visualization.run("draw");
	}
	
	public void setNodeLabelProvider(ColumnLabelProvider nodeLabelProvider) {
		this.nodeLabelProvider = nodeLabelProvider;
	}

	public synchronized void updateVisualization() {
		if (graph == null)
			return;

		System.out.println("updateVisualization()");
		
		aggregates = new HashMap<String, AggregateItem>();
		
		visualization.removeGroup(AGGREGATES);
		aggregateTable = visualization.addAggregates(AGGREGATES);
		aggregateTable.addColumn(VisualItem.POLYGON, float[].class);
		aggregateTable.addColumn("id", String.class);

		visualization.removeGroup(GRAPH);
		final VisualGraph vg = visualization.addGraph(GRAPH, graph);
		visualization.setValue(EDGES, null, VisualItem.INTERACTIVE,
				Boolean.FALSE);

		
//		visualization.setValue(NODES, null, VisualItem.SHAPE,
//                new Integer(Constants.SHAPE_ELLIPSE));
		
/*		TupleSet nodesGroup = visualization.getGroup(NODES);
		nodesGroup.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println("!!! "+evt.getPropertyName()+" "+evt.getSource()+": aggregate old = "+evt.getOldValue()+", now = "+evt.getNewValue());
			}
		});
		
		nodesGroup.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i) {
					System.out.println("--- removed "+rem[i]);
				}
				for (int i = 0; i < add.length; ++i) {
					System.out.println("+++ added "+add[i]);
				}
			}
		});

		vg.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i) {
					System.out.println("--- vg removed "+rem[i]);
				}
				for (int i = 0; i < add.length; ++i) {
					System.out.println("+++ vg added "+add[i]);
				}
			}
		});
*/		
		vg.getNodeTable().addTableListener(new TableListener() {
		    public void tableChanged(Table t, int start, int end, int col, int type) {
		    	if (col == -1)
		    		return;
				if (t.getColumnName(col).equals("aggregate"))
					for (int i=start; i<=end; i++)
						addNodeToAggregate((VisualItem) vg.getNodeTable().getTuple(i));
			}
		});
/*		vg.addGraphModelListener(new GraphListener() {
			public void graphChanged(Graph g, String table, int start, int end,
					int col, int type) {
				System.out.println("graph changed table="+table+" "+start+" "+end+" "+col+" "+type);
//				redraw(); // XXX is this necesary?
			}
		});
*/

		if (vg.getNodeCount() <= 0)
			return;

/*		Iterator tuples = vg.getNodes().tuples();
		while (tuples.hasNext()) {
			Tuple tuple = (Tuple)tuples.next();
			visualization.getGroup(Visualization.FOCUS_ITEMS).setTuple(tuple);
		}
*/
		// focus on the first node
		VisualItem f = (VisualItem) vg.getNode(0);
		visualization.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		
		// f.setFixed(false);
				
		// add nodes to aggregates
/*		Iterator<?> nodes = vg.nodes();
		while (nodes.hasNext()) {
			VisualItem node = (VisualItem) nodes.next();
			System.out.println("process "+node);
			addNodeToAggregate(node);
		}
*/	}
	
	private void addNodeToAggregate(VisualItem node) {
		if (!(node instanceof NodeItem))
			return;
		String aggregate = node.getString("aggregate");
		if (aggregate == null) return;
		AggregateItem aitem = aggregates.get(aggregate);
		if (aitem == null) {
//			visualization.getAction("layout").setEnabled(false);
//			visualization.getAction("draw").setEnabled(false);
			aitem = (AggregateItem) aggregateTable.addItem();
			aitem.set("id", aggregate);
			aggregates.put(aggregate, aitem);
//			visualization.getAction("layout").setEnabled(true);
//			visualization.getAction("draw").setEnabled(true);
		}
		aitem.addItem(node);
	}
	
	public void redraw() {
		// is this necesary?
//		visualization.run("layout");
		visualization.run("draw");
		embeddee.repaint();
	}

	// ------------------------------------------------------------------------

	public class FitOverviewListener implements ItemBoundsListener {
		private Rectangle2D m_bounds = new Rectangle2D.Double();
		private Rectangle2D m_temp = new Rectangle2D.Double();
		private double m_d = 15;

		public void itemBoundsChanged(Display d) {
			d.getItemBounds(m_temp);
			GraphicsLib.expand(m_temp, 25 / d.getScale());

			double dd = m_d / d.getScale();
			double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
			double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
			double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
			double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
			if (xd > dd || yd > dd || wd > dd || hd > dd) {
				m_bounds.setFrame(m_temp);
				DisplayLib.fitViewToBounds(d, m_bounds, 0);
			}
		}
	}
	
	public void addPrefuseControlListener(Control control) {
		((Display)embeddee).addControlListener(control);
	}

	public void setAnimationEnabled(boolean enabled) {
		if (forceLayout.isEnabled() == enabled)
			return;
		forceLayout.setEnabled(enabled);
//		((Display)embeddee).setHighQuality(!enabled);
		if (!enabled) redraw();
	}

	public boolean isAnimationEnabled() {
		return forceLayout != null && forceLayout.isEnabled();
	}

	public void setOverviewEnabled(boolean enabled) {
		if (overview != null) {
			overview.setVisible(enabled);
		} else if (enabled) {
			// overview display
			overview = new Display(visualization);
			overview.setLocation(10, 10);
			overview.setSize(100,100);
			overview.addItemBoundsListener(new FitOverviewListener());
			overview.setBorder(BorderFactory.createEtchedBorder());
			((Display)embeddee).add(overview);
		}
	}
	
	public boolean isOverviewEnabled() {
		return overview != null && overview.isVisible();
	}
	
	public void setLabelsEnabled(boolean enabled) {
		if (showLabels == enabled)
			return;
		showLabels = enabled;
		setRenderers();
		redraw();
	}
	
	public boolean isLabelsEnabled() {
		return showLabels;
	}

	public void setImagesEnabled(boolean enabled) {
		if (showImages == enabled)
			return;
		showImages = enabled;
		setRenderers();
		redraw();
	}
	
	public boolean isImagesEnabled() {
		return showImages;
	}

	public void setEnforceBounds(boolean enforceBounds) {
		if (this.enforceBounds == enforceBounds)
			return;
		this.enforceBounds = enforceBounds;
		setLayoutActions();
		redraw();
	}
	
	public boolean isEnforceBounds() {
		return enforceBounds;
	}

	public VisualItem getItem(Point point) {
		return ((Display)embeddee).findItem(new java.awt.Point(point.x,point.y));
	}
}
