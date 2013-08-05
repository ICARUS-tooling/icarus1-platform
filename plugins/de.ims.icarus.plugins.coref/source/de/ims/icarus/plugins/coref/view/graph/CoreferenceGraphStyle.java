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

import java.awt.Color;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxUtils;

import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.plugins.jgraph.layout.DefaultGraphStyle;
import de.ims.icarus.plugins.jgraph.layout.GraphOwner;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceGraphStyle extends DefaultGraphStyle {
	
	protected Color falseEdgeColor = Color.red;
	protected Color falseNodeColor = Color.red;

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
	protected void refreshStylesheet(Handle handle) {
		super.refreshStylesheet(handle);
		
		ConfigRegistry config = handle.getSource();
		falseEdgeColor = config.getColor(config.getChildHandle(handle, "falseEdgeColor")); //$NON-NLS-1$
		falseNodeColor = config.getColor(config.getChildHandle(handle, "falseNodeColor")); //$NON-NLS-1$
	}

	@Override
	public String getStyle(GraphOwner owner, Object cell, Options options) {
		mxIGraphModel model = owner.getGraph().getModel();
		Object value = model.getValue(cell);
		if(value instanceof CorefNodeData) {
			CorefNodeData data = (CorefNodeData) value;
			StringBuilder style = new StringBuilder("defaultVertex"); //$NON-NLS-1$

			if(data.isFalsePredicted() && getPresenter().isMarkFalseNodes()) {
				style.append(";strokeColor=") //$NON-NLS-1$
				.append(mxUtils.getHexColorString(falseNodeColor)); 
			}
			if(data.isMissingGold()) {
				style.append(";dashed=1"); //$NON-NLS-1$
			} 
			return style.toString();
		} else if(value instanceof CorefEdgeData) {
			CorefEdgeData data = (CorefEdgeData) value;
			StringBuilder style = new StringBuilder("defaultEdge"); //$NON-NLS-1$
			if(data.getEdge().getSource().isROOT()) {
				style.append(";shape=curveConnector"); //$NON-NLS-1$
			}
			if(data.isFalsePredicted() && getPresenter().isMarkFalseEdges()) {
				style.append(";strokeColor=") //$NON-NLS-1$
				.append(mxUtils.getHexColorString(falseEdgeColor)); 
			}
			if(data.isMissingGold()) {
				style.append(";dashed=1"); //$NON-NLS-1$
			} 
			if(!data.isMissingGold() || data.getEdge().getSource().isROOT()) {
				style.append(";entryX=0.5;entryY=0"); //$NON-NLS-1$
			}
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