/*
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

	public CoreferenceGraphStyle() {
		// no-op
	}

	@Override
	protected void initStylesheet() {
		super.initStylesheet();
		
		Map<String, Object> style = getStylesheet().getDefaultEdgeStyle();
		style.put("entryX", 0.5f); //$NON-NLS-1$
		style.put("entryY", 0.0f); //$NON-NLS-1$
	}

	@Override
	protected CoreferenceGraphPresenter getPresenter() {
		return (CoreferenceGraphPresenter) super.getPresenter();
	}

	@Override
	protected void refreshStylesheet(Handle handle) {
		super.refreshStylesheet(handle);
		
		ConfigRegistry config = handle.getSource();
		falseEdgeColor = config.getColor(config.getChildHandle(handle, "falseEdgeColor")); //$NON-NLS-1$
	}

	@Override
	public String getStyle(GraphOwner owner, Object cell, Options options) {
		Object value = owner.getGraph().getModel().getValue(cell);
		if(value instanceof CorefNodeData) {
			return "defaultVertex"; //$NON-NLS-1$
		} else if(value instanceof CorefEdgeData) {
			CorefEdgeData data = (CorefEdgeData) value;
			String style = "defaultEdge"; //$NON-NLS-1$
			if(data.getEdge().getSource().isROOT()) {
				style += ";shape=curveConnector"; //$NON-NLS-1$
			}
			if(data.isFalsePredictedEdge() && getPresenter().isMarkFalseEdges()) {
				style += ";strokeColor="+mxUtils.getHexColorString(falseEdgeColor); //$NON-NLS-1$
			} else if(data.isMissingGoldEdge() && getPresenter().isShowGoldEdges()) {
				style += ";dashed=1"; //$NON-NLS-1$
			}
			return style;
		} else {
			return super.getStyle(owner, cell, options);
		}
	}
}