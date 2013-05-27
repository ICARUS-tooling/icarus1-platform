/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view.graph;

import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxIGraphModel;

import net.ikarus_systems.icarus.plugins.jgraph.layout.DefaultGraphStyle;
import net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner;
import net.ikarus_systems.icarus.plugins.jgraph.util.GraphUtils;
import net.ikarus_systems.icarus.search_tools.NodeType;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintGraphStyle extends DefaultGraphStyle {
	
	protected String disjunctionNodeStyle;

	public ConstraintGraphStyle() {
		initStyles();
	}
	
	protected void initStyles() {
		disjunctionNodeStyle = "shape=ellipse;";
	}

	@Override
	public String getStyle(GraphOwner owner, Object cell, Options options) {
		Object value = GraphUtils.getNodeValue(owner, cell);
		ConstraintNodeData nodeData = value instanceof ConstraintNodeData ?
				(ConstraintNodeData) value : null;
		
		if(nodeData!=null && nodeData.getNodeType()==NodeType.DISJUNCTION) {
			return disjunctionNodeStyle;
		} else {
			return super.getStyle(owner, cell, options);
		}
	}
}
