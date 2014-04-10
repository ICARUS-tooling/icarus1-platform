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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view.graph;

import java.awt.Color;
import java.util.Map;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;

import de.ims.icarus.language.coref.annotation.CoreferenceDocumentAnnotationManager;
import de.ims.icarus.language.coref.annotation.CoreferenceDocumentHighlighting;
import de.ims.icarus.plugins.jgraph.layout.GraphRenderer;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphRenderer extends GraphRenderer {

	protected CoreferenceGraphPresenter presenter;

	public CoreferenceGraphRenderer() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		if(target instanceof CoreferenceGraphPresenter) {
			presenter = (CoreferenceGraphPresenter)target;
		}
	}

	/**
	 * @see de.ims.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		presenter = null;
	}

	protected void refreshCellStyle(mxCellState state) {
		if(presenter==null) {
			return;
		}
		if(presenter.getAnnotationManager()==null) {
			return;
		}
		CoreferenceDocumentAnnotationManager annotationManager = (CoreferenceDocumentAnnotationManager) presenter.getAnnotationManager();

		if(annotationManager.getDisplayMode()==AnnotationDisplayMode.NONE) {
			return;
		}

		if(!annotationManager.hasAnnotation()) {
			return;
		}

		mxIGraphModel model = state.getView().getGraph().getModel();
		Object cell = state.getCell();
		boolean isNode = model.isVertex(cell);
		if(!isNode) {

			if(presenter.isHighlightedIncomingEdge(cell)
					|| presenter.isHighlightedOutgoingEdge(cell)) {
				return;
			}

			cell = model.getTerminal(cell, false);
		}
		Object value = model.getValue(cell);

		if(!(value instanceof CorefNodeData)) {
			return;
		}
		CorefNodeData data = (CorefNodeData)value;

		if(data.getSpan().isROOT()) {
			return;
		}

		long highlight = annotationManager.getHighlight(presenter.getIndex(data.getSpan()));

		//System.out.println(data.getIndex()+":"+DependencyHighlighting.dumpHighlight(highlight));

		Color color = null;
		int groupId = isNode ?
				CoreferenceDocumentHighlighting.getInstance().getNodeGroupId(highlight)
				: CoreferenceDocumentHighlighting.getInstance().getEdgeGroupId(highlight);
		if(groupId!=-1) {
			color = Grouping.getGrouping(groupId).getColor();
		} else {
			color = isNode ?
					CoreferenceDocumentHighlighting.getInstance().getNodeHighlightColor(highlight)
					: CoreferenceDocumentHighlighting.getInstance().getEdgeHighlightColor(highlight);
		}

		if(color!=null) {
			Map<String, Object> style = state.getStyle();
			style.put(mxConstants.STYLE_STROKECOLOR, mxUtils.hexString(color));
		}
	}

	@Override
	public Object drawCell(mxCellState state) {
		refreshCellStyle(state);

		/*System.out.println(((mxCell)state.getCell()).getValue());
		System.out.print(CollectionUtils.toString(state.getStyle()));
		System.out.println();*/

		return super.drawCell(state);
	}
}
