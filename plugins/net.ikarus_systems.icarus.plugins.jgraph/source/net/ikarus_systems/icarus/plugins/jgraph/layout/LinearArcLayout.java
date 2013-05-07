/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.layout;

import java.util.List;

import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.Options;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LinearArcLayout implements GraphLayout, GraphLayoutConstants {
	
	protected String defaultEdgeStyle;
	protected String orderEdgeStyle;
	protected String ltrEdgeStyle;
	protected String rtlEdgeStyle;

	public LinearArcLayout() {
		initStyles();
	}
	
	protected void initStyles() {
		defaultEdgeStyle = ";exitY=0.0;entryY=0.0;edgeStyle=topArcEdgeStyle"; //$NON-NLS-1$
		orderEdgeStyle = ";exitY=1.0;entryY=1.0;edgeStyle=bottomArcEdgeStyle"; //$NON-NLS-1$
		
		ltrEdgeStyle = ";exitX=0.5;entryX=0.35"; //$NON-NLS-1$
		rtlEdgeStyle = ";exitX=0.5;entryX=0.65"; //$NON-NLS-1$
	}

	@SuppressWarnings("unused")
	protected mxRectangle doLayout(GraphOwner owner, Object[] cells, Options options) {
		mxGraph graph = owner.getGraph();
		
		int cellSpacing = options.get(CELL_SPACING_KEY, graph.getGridSize()*2);
		int topInsets = options.get(TOP_INSETS_KEY, 20);
		int bottomInsets = options.get(BOTTOM_INSETS_KEY, 20);
		int leftInsets = options.get(LEFT_INSETS_KEY, 20);
		int rightInsets = options.get(RIGHT_INSETS_KEY, 20);
		int minBaseline = options.get(MIN_BASELINE_KEY, DEFAULT_MIN_BASELINE);

		mxIGraphModel model = graph.getModel();
		
		mxGeometry[] geometries = new mxGeometry[cells.length];
		
		mxRectangle bounds = new mxRectangle();

		model.beginUpdate();
		try {
			double x = 0;
			double y = 0;
			
			double width = 0;
			double height = 0;
			
			double topArcHeight = 0;
			double bottomArcHeight = 0;
			
			for(int i=0; i<cells.length; i++) {
				Object cell = cells[i];
				
				// Refresh cell size if graph is auto-sizing
				if(graph.isAutoSizeCell(cell)) {
					graph.cellSizeUpdated(cell, false);
				}
				
				// Move cell to new location
				mxGeometry geometry = (mxGeometry) model.getGeometry(cell).clone();
				geometry.setX(x);
				geometry.setY(y);
				
				// Shift horizontal offset
				x += geometry.getWidth()+cellSpacing;
				
				if(i>0) {
					width += cellSpacing;
				}
				width += geometry.getWidth();
				height = Math.max(height, geometry.getHeight());
				
				// Save geometry
				geometries[i] = geometry;
				
				// Calculate highest arc
				for(Object edge : graph.getOutgoingEdges(cell)) {
					mxGeometry targetGeometry = model.getGeometry(model.getTerminal(edge, false));
					double span = Math.abs(geometry.getCenterX() - targetGeometry.getCenterX());
							
					// Apply style
					String style = model.getStyle(edge);
					if(GraphUtils.isOrderEdge(graph, edge)) {
						bottomArcHeight = Math.max(bottomArcHeight, ArcConnectorShape.getArcHeight(span));
						style += orderEdgeStyle;
					} else {
						topArcHeight = Math.max(topArcHeight, ArcConnectorShape.getArcHeight(span));
						style += defaultEdgeStyle;
					}
					
					if(GraphUtils.isLtrEdge(graph, edge)) {
						style += ltrEdgeStyle;
					} else {
						style += rtlEdgeStyle;
					}
					
					String oldStyle = model.getStyle(edge);
					if(oldStyle==null || !oldStyle.endsWith(style)) {
						model.setStyle(edge, style);
					}
				}
			}
			
			// Now relocate the entire graph
			double offsetY = topInsets + Math.max(minBaseline, topArcHeight);
			double offsetX = leftInsets;
			
			for(int i=0; i<cells.length; i++) {
				mxGeometry geometry = geometries[i];
				geometry.setX(geometry.getX()+offsetX);
				geometry.setY(geometry.getY()+offsetY);
				model.setGeometry(cells[i], geometry);
			}
			
			bounds.setHeight(Math.max(minBaseline, topArcHeight)+height+bottomArcHeight);
			bounds.setWidth(width);
		} finally {
			model.endUpdate();
		}
		
		return bounds;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(com.mxgraph.view.mxGraph, java.lang.Object[], net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void layoutGraph(GraphOwner owner, Object[] cells, Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		doLayout(owner, cells, options);
	}

	/**
	 * Tries to find a vertex whose parent is a vertex 
	 * right beside it.
	 * @param vertices
	 * @return
	 */
	protected Object findShrinkableVertex(mxGraph graph, List<Object> vertices, Filter filter) {
		// TODO request: do not shrink subgraph with highlighted root
		
		int index = vertices.size();

		while(--index>=0) {
			Object vertex = vertices.get(index);
			
			// Only shrink leaf nodes
			if(graph.getOutgoingEdges(vertex).length>0)
				continue;
			
			// Allow filtering of important nodes (highlighted or whatsoever)
			if(filter.accepts(vertex))
				continue;
			
			// Check left node
			Object head = index>0 ? vertices.get(index-1) : null;
			if(head!=null && getHeadNode(graph, vertex)==head) {
				return vertex;
			}
			
			// Check Right node
			head = index<vertices.size()-1 ? vertices.get(index+1) : null;
			if(head!=null && getHeadNode(graph, vertex)==head) {
				return vertex;
			}
		}
		
		return null;
	}
	
	protected Object getHeadNode(mxGraph graph, Object node) {
		mxIGraphModel model = graph.getModel();
		
		for(Object edge : graph.getIncomingEdges(node))
			if(!(GraphUtils.isOrderEdge(graph, edge)))
				return model.getTerminal(edge, true);
		
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(com.mxgraph.view.mxGraph, java.lang.Object[], net.ikarus_systems.icarus.util.Options, net.ikarus_systems.icarus.util.Filter, com.mxgraph.util.mxRectangle)
	 */
	@SuppressWarnings("unused")
	@Override
	public void compressGraph(GraphOwner owner, Object[] cells, Options options,
			Filter filter, mxRectangle bounds) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		mxGraph graph = owner.getGraph();
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {
			mxRectangle size = doLayout(owner, cells, options);
			
			// Do not bother with compression if already sufficient
			if(size.getWidth()<=bounds.getWidth()) {
				return;
			}
			
			double width = size.getWidth();
			double height = size.getHeight();
			
			CellMerger merger = (CellMerger) options.get(CELL_MERGER_KEY);
			int cellSpacing = options.get(CELL_SPACING_KEY, graph.getGridSize()*2);
			int topInsets = options.get(TOP_INSETS_KEY, 20);
			int bottomInsets = options.get(BOTTOM_INSETS_KEY, 20);
			int leftInsets = options.get(LEFT_INSETS_KEY, 20);
			int rightInsets = options.get(RIGHT_INSETS_KEY, 20);
			int minBaseline = options.get(MIN_BASELINE_KEY, DEFAULT_MIN_BASELINE);
			List<Object> vertices = CollectionUtils.asList(cells);
			
			while(size.getWidth()>bounds.getWidth()) {

				if(vertices.size()==1)
					break;
			
				// Find a vertex suitable for shrinking
				Object vertex = findShrinkableVertex(graph, vertices, filter);
				
				// Stop shrinking if no suitable node could be found
				// this might be the case if ALL nodes are filtered out
				// or the graph has some rare crossed over structure
				if(vertex==null)
					break;
				
				// Shrink our list
				vertices.remove(vertex);
				Object head = getHeadNode(graph, vertex);				
				double widthBefore = model.getGeometry(head).getWidth(); 
				
				// Merge item as child of head node
				// and recalculate size
				double widthAfter = widthBefore;
				if(merger!=null) {
					merger.merge(owner, head, vertex);
					graph.cellLabelChanged(head, model.getValue(head), 
							graph.isAutoSizeCell(head));
					widthAfter = model.getGeometry(head).getWidth();
				}
				
				// Subtract our "saved" width from the current total width
				size.setWidth(size.getWidth() 
						- model.getGeometry(vertex).getWidth()
						- cellSpacing
						- widthBefore + widthAfter);
				
				// Remove vertex and edges between
				model.remove(vertex);
				for(Object edge : graph.getEdgesBetween(head, vertex)) {
					model.remove(edge);
				}
			}

			// If we could shrink the graph refresh layout
			double maxHeight = 0;
			if(width!=size.getWidth()) {
				double x = leftInsets;
				for(Object cell : vertices) {
					mxGeometry geo = model.getGeometry(cell);
					maxHeight = Math.max(maxHeight, geo.getHeight());
					
					geo.setX(x);
					
					x += geo.getWidth() + cellSpacing;
				}
			}
			
			// Fetch new bounding box
			// we need this to refresh height
			size = graph.getView().getGraphBounds();
			
			// Move all nodes up if we have to
			if(height != size.getHeight()) {
				double y = topInsets + size.getHeight() - maxHeight;
				// honor the general minimum insets for graphs
				y = Math.max(y, minBaseline);
				
				for(Object cell : vertices) {
					model.getGeometry(cell).setY(y);
				}
			}
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(GraphOwner target) {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(GraphOwner target) {
		// no-op
	}
}
