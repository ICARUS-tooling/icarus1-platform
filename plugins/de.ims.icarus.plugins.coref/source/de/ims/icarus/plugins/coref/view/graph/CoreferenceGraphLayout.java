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
package de.ims.icarus.plugins.coref.view.graph;

import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.plugins.jgraph.layout.GraphLayout;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.plugins.jgraph.util.GraphUtils;
import de.ims.icarus.plugins.jgraph.view.GraphPresenter;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphLayout implements GraphLayout {

	protected mxIGraphLayout layout;

	public CoreferenceGraphLayout() {
		// no-op
	}

	protected mxIGraphLayout createLayout(mxGraph graph) {
		return new TreeLayout(graph);
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof GraphPresenter) {
			layout = createLayout(((GraphPresenter)target).getGraph());
		} else {
			layout = null;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		layout = null;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#getEdgeStyle(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public String getEdgeStyle(GraphOwner owner, Object edge, Options options) {
		String style = owner.getGraph().getModel().getStyle(edge);
		return style==null ? "defaultEdge" : style; //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options)
	 */
	@Override
	public mxRectangle layoutGraph(GraphOwner owner, Object[] cells,
			Options options) {
		if(layout==null) {
			return owner.getGraph().getGraphBounds();
		}

		mxGraph graph = owner.getGraph();
		Object parent = graph.getDefaultParent();


		graph.getModel().beginUpdate();
		try {
			for(Object cell : cells) {
				graph.cellSizeUpdated(cell, false);
			}

			layout.execute(parent);
//
//			double offset = graph.getGridSize()*2;
//			graph.moveCells(cells, offset, offset);
		} finally {
			graph.getModel().endUpdate();
		}

		return graph.getGraphBounds();
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options, com.mxgraph.util.mxRectangle)
	 */
	@Override
	public mxRectangle compressGraph(GraphOwner owner, Object[] cells,
			Options options, mxRectangle bounds) {
		// TODO apply compression
		return layoutGraph(owner, cells, options);
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#getSignificantCell(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options)
	 */
	@Override
	public Object getSignificantCell(GraphOwner owner, Object[] cells,
			Options options) {

		if(cells==null || cells.length==0) {
			return null;
		}

		CoreferenceGraphPresenter presenter = (CoreferenceGraphPresenter) owner;
		mxIGraphModel model = owner.getGraph().getModel();

		Filter filter = presenter.getfFilter();

		if(filter!=null) {
			for(Object cell : cells) {
				if(!model.isVertex(cell)) {
					continue;
				}

				CorefNodeData value = (CorefNodeData) model.getValue(cell);
				if(filter.accepts(value.getSpan())) {
					return cell;
				}
			}
		}

		CoreferenceDocumentAnnotationManager annotationManager = (CoreferenceDocumentAnnotationManager) presenter.getAnnotationManager();

		for(Object cell : cells) {
			if(!model.isVertex(cell)) {
				continue;
			}

			CorefNodeData value = (CorefNodeData) model.getValue(cell);
			if(value.getSpan().isROOT()) {
				continue;
			}

			long highlight = annotationManager.getHighlight(presenter.getIndex(value.getSpan()));

			if(CoreferenceDocumentHighlighting.getInstance().isHighlighted(highlight)) {
				return cell;
			}
		}

		return cells[0];
	}

	protected class TreeLayout extends mxCompactTreeLayout {

		public TreeLayout(mxGraph graph) {
			super(graph, false);

			setEdgeRouting(false);
			setNodeDistance(5);
			setLevelDistance(15);
			setMoveTree(true);
		}

		@Override
		public boolean isEdgeIgnored(Object edge) {
			boolean ignored = super.isEdgeIgnored(edge);

			if(ignored) {
				return true;
			}

			mxIGraphModel model = graph.getModel();
			Object value = model.getValue(edge);
			if(value instanceof CorefEdgeData) {
				CorefEdgeData data = (CorefEdgeData) value;
				Object target = model.getTerminal(edge, false);
				CorefNodeData nodeData = (CorefNodeData) model.getValue(target);

				if(data.getEdge().getSource().isROOT()) {
					return false;
				}

				if(GraphUtils.getIncomingEdgeCount(model, target)==1) {
					return false;
				}

				return (nodeData.isGold() || data.isGold());
			}

			return false;
		}

	}
}
