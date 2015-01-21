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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultTreeLayout implements GraphLayout {

	protected String regularEdgeStyle;
	protected String orderEdgeStyle;

	public DefaultTreeLayout() {
		initStyles();
	}

	protected void initStyles() {
		regularEdgeStyle = ";exitY=1.0;entryY=0.0;exitX=0.5;entryX=0.5;shape=connector"; //$NON-NLS-1$
		orderEdgeStyle = ";exitY=1.0;entryY=1.0;exitX=0.5;entryX=0.5;edgeStyle=bottomArcEdgeStyle;shape=arc;dashed=1"; //$NON-NLS-1$
	}

	protected mxRectangle layoutCell(GraphOwner owner, Object cell,
			Set<Object> allowedCells, double x, double y, double vGap, double hGap) {
		mxGraph graph = owner.getGraph();
		mxRectangle bounds;

		// Refresh cell size if graph is auto-sizing
		if(graph.isAutoSizeCell(cell)) {
			graph.cellSizeUpdated(cell, false);
		}

		mxIGraphModel model = graph.getModel();
		mxGeometry geometry = model.getGeometry(cell);
		geometry.setX(x);
		geometry.setY(y);

		bounds = new mxRectangle(geometry);

		Object[] children = GraphUtils.getDescendants(owner, cell);
		if(children!=null) {

			y += geometry.getHeight()+vGap;

			for(Object child : children) {
				if(!allowedCells.contains(child)) {
					continue;
				}

				mxRectangle childBounds = layoutCell(owner, child,
						allowedCells, x, y, vGap, hGap);

				x += childBounds.getWidth()+hGap;

				bounds.add(childBounds);
			}

			if(bounds.getWidth()>geometry.getWidth()) {
				double offset = (bounds.getWidth()-geometry.getWidth())/2;
				geometry.setX(geometry.getX()+offset);
			}
		}

		return bounds;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(com.mxgraph.view.mxGraph, java.lang.Object[])
	 */
	@Override
	public mxRectangle layoutGraph(GraphOwner owner, Object[] cells, Options options) {
		if(options==null) {
			options = Options.emptyOptions;
		}

		mxGraph graph = owner.getGraph();

		int cellSpacing = options.get(CELL_SPACING_KEY, graph.getGridSize()*2);
		double offsetX = options.get(OFFSET_X_KEY, (double)cellSpacing);
		double offsetY = options.get(OFFSET_Y_KEY, (double)cellSpacing);

		mxIGraphModel model = graph.getModel();

		Set<Object> allowedCells = CollectionUtils.asSet(cells);

		mxRectangle bounds = new mxRectangle(offsetX, offsetY, 0, 0);

		model.beginUpdate();
		try {

			double x = offsetX;
			double y = offsetY;
			double vGap = 2*cellSpacing;
			double hGap = cellSpacing;

			// Clone all geometries and assign edge styles
			for(Object cell : cells) {
				mxGeometry geometry = model.getGeometry(cell);
				geometry = (mxGeometry) geometry.clone();
				model.setGeometry(cell, geometry);

				int edgeCount = model.getEdgeCount(cell);
				for(int i=0; i<edgeCount; i++) {
					Object edge = model.getEdgeAt(cell, i);

					// Only handle outgoing edges here
					if(model.getTerminal(edge, false)==cell) {
						continue;
					}
					model.setStyle(edge, getEdgeStyle(owner, edge, options));
				}
			}


			// Fetch roots (only within supplied cells array!)
			Object[] rootCells = GraphUtils.getRootCells(model, cells);

			// Arrange all cells
			for(Object cell : rootCells) {
				mxRectangle rootBounds = layoutCell(owner, cell, allowedCells,
						x, y, vGap, hGap);

				x += rootBounds.getWidth() + hGap;

				bounds.add(rootBounds);
			}

		} finally {
			model.endUpdate();
		}

		return bounds;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(com.mxgraph.view.mxGraph, java.lang.Object[], com.mxgraph.util.mxRectangle)
	 */
	@Override
	public mxRectangle compressGraph(GraphOwner owner, Object[] cells, Options options, mxRectangle bounds) {
		return layoutGraph(owner, cells, options);
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
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#getSignificantCell(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options)
	 */
	@Override
	public Object getSignificantCell(GraphOwner owner, Object[] cells,
			Options options) {
		cells = GraphUtils.getRootCells(owner.getGraph().getModel(), cells);
		return cells==null || cells.length==0 ? null : cells[0];
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

	protected static class TreeBuffer {
		protected int maxDepth = 0;
		protected Map<Integer, List<Object>> leafs;

		public void add(Object leaf, int depth) {
			if(depth<0) {
				return;
			}

			if(leafs==null) {
				leafs = new HashMap<>();
			}

			List<Object> list = leafs.get(depth);
			if(list==null) {
				list = new LinkedList<>();
				leafs.put(depth, list);
			}

			maxDepth = Math.max(depth, maxDepth);
		}

		public void remove(Object leaf, int depth) {
			if(leafs==null) {
				return;
			}

			List<Object> list = leafs.get(depth);
			if(list==null) {
				return;
			}

			list.remove(leaf);

			if(list.isEmpty() && depth==maxDepth) {
				leafs.remove(depth);
				while(maxDepth>0 && !leafs.containsKey(maxDepth)) {
					maxDepth--;
				}
			}
		}

		public int getMaxDepths() {
			return maxDepth;
		}

		public List<Object> getLeafsForDepths(int depth) {
			return leafs==null ? null : leafs.get(depth);
		}

		public void moveUp(Object leaf, int depth) {
			remove(leaf, depth);
			add(leaf, depth-1);
		}
	}
}
