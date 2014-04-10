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

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxUtils;

import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.plugins.jgraph.layout.DefaultGraphStyle;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphStyle extends DefaultGraphStyle {

	public CoreferenceGraphStyle() {
		// no-op
	}

	/*@Override
	protected void initStylesheet() {
		super.initStylesheet();

		Map<String, Object> style = getStylesheet().getDefaultEdgeStyle();
		style.put("entryX", 0.5f); //$NON-NLS-1$
		style.put("entryY", 0.0f); //$NON-NLS-1$
	}*/

	@Override
	protected CoreferenceGraphPresenter getPresenter() {
		return (CoreferenceGraphPresenter) super.getPresenter();
	}

	@Override
	public String getStyle(GraphOwner owner, Object cell, Options options) {
		mxIGraphModel model = owner.getGraph().getModel();
		Object value = model.getValue(cell);
		long highlight = 0L;

//		if(value instanceof CorefCellData) {
//			highlight = ((CorefCellData<?>)value).getHighlight();
//
//			if(CoreferenceDocumentHighlighting.getInstance().isHighlighted(highlight)) {
//				Color col = CoreferenceDocumentHighlighting.getInstance().getGroupColor(highlight);
//				if(col==null) {
//					col = CoreferenceDocumentHighlighting.getInstance().getHighlightColor(highlight);
//				}
//
//				if(col!=null) {
//					return "defaultVertex;strokeColor="+mxUtils.getHexColorString(col); //$NON-NLS-1$
//				}
//			}
//		}
		// TODO merge color inference from both parts

		if(value instanceof CorefNodeData) {
			CorefNodeData data = (CorefNodeData) value;
			StringBuilder style = new StringBuilder("defaultVertex"); //$NON-NLS-1$

			CorefErrorType errorType = data.getErrorType();

			if(data.getSpan().isROOT()) {
				style.append(";strokeColor=#969696"); //$NON-NLS-1$
			} else if(errorType!=null && errorType!=CorefErrorType.TRUE_POSITIVE_MENTION
					&& getPresenter().isMarkFalseNodes()) {
				style.append(";strokeColor=") //$NON-NLS-1$
				.append(mxUtils.getHexColorString(CoreferenceUtils.getErrorColor(errorType)));
			}
			if(data.isGold()) {
				style.append(";dashed=1"); //$NON-NLS-1$
			}
			return style.toString();
		} else if(value instanceof CorefEdgeData) {
			CorefEdgeData data = (CorefEdgeData) value;
			CorefNodeData nodeData = (CorefNodeData) model.getValue(model.getTerminal(cell, false));
			CorefErrorType errorType = nodeData.getErrorType();

			StringBuilder style = new StringBuilder("defaultEdge"); //$NON-NLS-1$
			if(data.getEdge().getSource().isROOT()) {
				style.append(";shape=curveConnector;strokeColor=#969696"); //$NON-NLS-1$
			}
			if(errorType!=null && errorType.isEdgeRelated()
					&& getPresenter().isMarkFalseEdges()) {
				style.append(";strokeColor=") //$NON-NLS-1$
				.append(mxUtils.getHexColorString(CoreferenceUtils.getErrorColor(errorType)));
			}
			if(data.isGold()) {
				style.append(";dashed=1"); //$NON-NLS-1$
			}
//			if(!data.isGold() || data.getEdge().getSource().isROOT()) {
				style.append(";entryX=0.5;entryY=0"); //$NON-NLS-1$
//			}
			if(getPresenter().isHighlightedIncomingEdge(cell)) {
				style.append(";strokeColor=") //$NON-NLS-1$
				.append(mxUtils.getHexColorString(getPresenter().getIncomingEdgeColor()));
			} else if(getPresenter().isHighlightedOutgoingEdge(cell)) {
				style.append(";strokeColor=") //$NON-NLS-1$
				.append(mxUtils.getHexColorString(getPresenter().getOutgoingEdgeColor()));
			}
			return style.toString();
		} else {
			return super.getStyle(owner, cell, options);
		}
	}
}