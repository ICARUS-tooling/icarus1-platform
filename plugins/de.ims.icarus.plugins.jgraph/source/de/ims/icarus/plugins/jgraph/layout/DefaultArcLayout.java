/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.util.CollectionUtils;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultArcLayout implements GraphLayout {
	
	protected String regularEdgeStyle;
	protected String orderEdgeStyle;
	protected String defaultConnectionStyle;
	protected String ltrConnectionStyle;
	protected String rtlConnectionStyle;

	public DefaultArcLayout() {
		initStyles();
	}
	
	protected void initStyles() {
		regularEdgeStyle = ";exitY=0.0;entryY=0.0;edgeStyle=topArcEdgeStyle;shape=arc"; //$NON-NLS-1$
		orderEdgeStyle = ";exitY=1.0;entryY=1.0;edgeStyle=bottomArcEdgeStyle;shape=arc;dashed=1"; //$NON-NLS-1$
		
		defaultConnectionStyle = ";exitX=0.5;entryX=0.5"; //$NON-NLS-1$
		ltrConnectionStyle = ";exitX=0.65;entryX=0.5"; //$NON-NLS-1$
		rtlConnectionStyle = ";exitX=0.35;entryX=0.5"; //$NON-NLS-1$
	}

	protected mxRectangle doLayout(GraphOwner owner, Object[] cells, Options options) {
		mxGraph graph = owner.getGraph();
		
		int cellSpacing = options.get(CELL_SPACING_KEY, graph.getGridSize()*2);
		int minBaseline = options.get(MIN_BASELINE_KEY, DEFAULT_MIN_BASELINE);
		double offsetX = options.get(OFFSET_X_KEY, cellSpacing);
		double offsetY = options.get(OFFSET_Y_KEY, cellSpacing);

		mxIGraphModel model = graph.getModel();
		
		Map<Object, mxGeometry> geometries = new HashMap<>();
		
		mxRectangle bounds = new mxRectangle();

		model.beginUpdate();
		try {
			double x = offsetX;
			double y = offsetY;
			
			double width = 0;
			double height = 0;
			
			double topArcHeight = 0;
			double bottomArcHeight = 0;
			
			// Layout cells
			for(int i=0; i<cells.length; i++) {
				Object cell = cells[i];
				
				if(model.isEdge(cell)) {
					continue;
				}
				
				// Refresh cell size if graph is auto-sizing
				if(graph.isAutoSizeCell(cell)) {
					graph.cellSizeUpdated(cell, false);
				}
				
				// Move cell to new location
				mxGeometry geometry = (mxGeometry) model.getGeometry(cell).clone();
				geometry.setX(graph.snap(x));
				geometry.setY(graph.snap(y));
				
				// Shift horizontal offset
				x += geometry.getWidth()+cellSpacing;
				
				if(i>0) {
					width += cellSpacing;
				}
				width += geometry.getWidth();
				height = Math.max(height, geometry.getHeight());
				
				// Save geometry
				geometries.put(cell, geometry);
			}
			
			for(Object cell : cells) {
				
				if(model.isEdge(cell)) {
					continue;
				}
				
				mxGeometry geometry = geometries.get(cell);
				
				// Calculate highest arc and assign styles
				for(Object edge : graph.getOutgoingEdges(cell)) {
					mxGeometry targetGeometry = geometries.get(model.getTerminal(edge, false));
					
					double span = Math.abs(geometry.getCenterX() - targetGeometry.getCenterX());
							
					boolean isOrder = GraphUtils.isOrderEdge(owner, model, edge);
					boolean isLink = GraphUtils.isLinkEdge(owner, edge);
					
					// Fetch basic style
					String style;
					if(isOrder || isLink) {
						bottomArcHeight = Math.max(bottomArcHeight, ArcConnectorShape.getArcHeight(span));
						style = orderEdgeStyle;
					} else {
						topArcHeight = Math.max(topArcHeight, ArcConnectorShape.getArcHeight(span));
						style = regularEdgeStyle;
					}
					
					// Append edge direction specific exit and entry
					if(!isLink) {
						if(GraphUtils.isLtrEdge(model, edge)) {
							style += ltrConnectionStyle;
						} else {
							style += rtlConnectionStyle;
						}
					}
					
					// Fetch old style and ensure non-null
					String oldStyle = model.getStyle(edge);
					if(oldStyle==null) {
						oldStyle = "defaultEdge"; //$NON-NLS-1$
					}
					
					// Since the GraphLayout is the last to modify
					// a cells style we can use suffix-equality to
					// check whether the required style is already set
					if(!oldStyle.endsWith(style)) {
						model.setStyle(edge, oldStyle+style);
					}
				}
			}
			
			// Now relocate the entire graph southwards
			double offset = Math.max(minBaseline, topArcHeight+offsetY);
			
			// Modifications take place on the previously created (cloned)
			// geometry objects, no need to clone them again!
			for(Object cell : cells) {
				if(model.isEdge(cell)) {
					continue;
				}
				
				mxGeometry geometry = geometries.get(cell);
				
				geometry.setY(graph.snap(offset));
				model.setGeometry(cell, geometry);
			}
			
			// Calculate total bounds
			bounds.setHeight(topArcHeight+height+bottomArcHeight);
			bounds.setWidth(width);
		} finally {
			model.endUpdate();
		}
		
		return bounds;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(com.mxgraph.view.mxGraph, java.lang.Object[], de.ims.icarus.util.Options)
	 */
	@Override
	public mxRectangle layoutGraph(GraphOwner owner, Object[] cells, Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		return doLayout(owner, cells, options);
	}

	/**
	 * Tries to find a vertex whose parent is a vertex 
	 * right beside it.
	 */
	protected Object findShrinkableVertex(GraphOwner owner, List<Object> vertices, Filter filter) {
		// TODO request: do not shrink subgraph with highlighted root
		
		int index = vertices.size();
		mxGraph graph = owner.getGraph();

		while(--index>=0) {
			Object vertex = vertices.get(index);
			
			// Only shrink leaf nodes
			if(graph.getOutgoingEdges(vertex).length>0)
				continue;
			
			// Allow filtering of important nodes (highlighted or whatsoever)
			if(filter!=null && filter.accepts(vertex))
				continue;
			
			// Check left node
			Object head = index>0 ? vertices.get(index-1) : null;
			if(head!=null && getLeafHeadNode(owner, vertex)==head) {
				return vertex;
			}
			
			// Check Right node
			head = index<vertices.size()-1 ? vertices.get(index+1) : null;
			if(head!=null && getLeafHeadNode(owner, vertex)==head) {
				return vertex;
			}
		}
		
		return null;
	}
	
	protected Object getLeafHeadNode(GraphOwner owner, Object node) {
		mxGraph graph = owner.getGraph();
		mxIGraphModel model = graph.getModel();
	
		// The only non-order edges can be incoming since we
		// only bother with shrinking leaf nodes
		for(int i=model.getEdgeCount(node)-1; i>-1; i--) {
			Object edge = model.getEdgeAt(node, i);
			if(!GraphUtils.isOrderEdge(owner, model, edge)) {
				return model.getTerminal(edge, true);
			}
		}
		
		return null;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(com.mxgraph.view.mxGraph, java.lang.Object[], de.ims.icarus.util.Options, de.ims.icarus.util.Filter, com.mxgraph.util.mxRectangle)
	 */
	@Override
	public mxRectangle compressGraph(GraphOwner owner, Object[] cells, Options options, mxRectangle bounds) {
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		mxGraph graph = owner.getGraph();
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {
			mxRectangle size = doLayout(owner, cells, options);
			
			// Do not bother with compression if already sufficient
			if(size.getWidth()<=bounds.getWidth() && size.getHeight()<bounds.getHeight()) {
				return size;
			}
			
			CellMerger merger = (CellMerger) options.get(CELL_MERGER_KEY);
			Filter filter = (Filter) options.get(CELL_FILTER_KEY);
			int cellSpacing = options.get(CELL_SPACING_KEY, graph.getGridSize()*2);
			int minBaseline = options.get(MIN_BASELINE_KEY, DEFAULT_MIN_BASELINE);
			double offsetX = options.get(OFFSET_X_KEY, cellSpacing);
			double offsetY = options.get(OFFSET_Y_KEY, cellSpacing);
			List<Object> vertices = CollectionUtils.asList(cells);
			
			while(size.getWidth()>bounds.getWidth()) {

				if(vertices.size()==1)
					break;
			
				// Find a vertex suitable for shrinking
				Object vertex = findShrinkableVertex(owner, vertices, filter);
				
				// Stop shrinking if no suitable node could be found
				// this might be the case if ALL nodes are filtered out
				// or the graph has some rare crossed over structure
				if(vertex==null)
					break;
				
				// Shrink our list
				vertices.remove(vertex);
				Object head = getLeafHeadNode(owner, vertex);				
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
			double x = offsetX;
			for(Object cell : vertices) {
				mxGeometry geo = model.getGeometry(cell);
				maxHeight = Math.max(maxHeight, geo.getHeight());
				
				geo.setX(x);
				
				x += geo.getWidth() + cellSpacing;
			}
			
			// Recalculate arc heights so we can fit the graph properly
			
			double topArcHeight = 0;
			double bottomArcHeight = 0;

			for(Object cell : vertices) {
				mxGeometry geometry = model.getGeometry(cell);
				
				// Calculate highest arc
				for(Object edge : graph.getOutgoingEdges(cell)) {
					mxGeometry targetGeometry = model.getGeometry(model.getTerminal(edge, false));
					double span = Math.abs(geometry.getCenterX() - targetGeometry.getCenterX());
							
					// Fetch basic style
					if(GraphUtils.isOrderEdge(model, edge)) {
						bottomArcHeight = Math.max(bottomArcHeight, ArcConnectorShape.getArcHeight(span));
					} else {
						topArcHeight = Math.max(topArcHeight, ArcConnectorShape.getArcHeight(span));
					}
				}
			}
			
			// Start with maximum offset
			double y = Math.max(offsetY + topArcHeight, minBaseline);
			
			// Squeeze in bottom arc if required
			if(y+maxHeight+bottomArcHeight>bounds.getHeight()) {
				y = bounds.getHeight()-maxHeight-bottomArcHeight;
			}
			
			// Squeeze in nodes if required
			if(y+maxHeight>bounds.getHeight()) {
				y = bounds.getHeight()-maxHeight;
			}
			
			// Ensure we do not clip the upper arcs
			y = Math.max(y, offsetY+topArcHeight);
			
			// Now move all nodes
			for(Object cell : vertices) {
				model.getGeometry(cell).setY(y);
			}
			
			return size;
		} finally {
			model.endUpdate();
		}
		
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#getEdgeStyle(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public String getEdgeStyle(GraphOwner owner, Object edge, Options options) {
		mxIGraphModel model = owner.getGraph().getModel();
		
		boolean isLink = GraphUtils.isLinkEdge(owner, edge);
		boolean isOrder = GraphUtils.isOrderEdge(owner, model, edge);
		
		String style;
		if(isOrder || isLink) {
			style = orderEdgeStyle;
		} else {
			style = regularEdgeStyle;
		}
		
		if(!isLink) {
			// Append edge direction specific exit and entry
			if(GraphUtils.isLtrEdge(model, edge)) {
				style += ltrConnectionStyle;
			} else {
				style += rtlConnectionStyle;
			}
		}
		
		// Fetch old style and ensure non-null
		String oldStyle = model.getStyle(edge);
		if(oldStyle==null || oldStyle.isEmpty()) {
			oldStyle = "defaultEdge"; //$NON-NLS-1$
		}
		
		// Since the GraphLayout is the last to modify
		// a cells style we can use suffix-equality to
		// check whether the required style is already set
		if(!oldStyle.endsWith(style)) {
			return oldStyle+style;
		}
		
		return oldStyle;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#getSignificantCell(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options)
	 */
	@Override
	public Object getSignificantCell(GraphOwner owner, Object[] cells,
			Options options) {
		return cells==null || cells.length==0 ? null : cells[0];
	}
}
