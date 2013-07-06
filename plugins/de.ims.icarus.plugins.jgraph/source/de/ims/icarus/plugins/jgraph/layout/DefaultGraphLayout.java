/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.jgraph.layout;


import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultGraphLayout implements GraphLayout {

	public DefaultGraphLayout() {
		// no-op
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
		String style = owner.getGraph().getModel().getStyle(edge);
		if(style==null || style.isEmpty()) {
			style = "defaultEdge"; //$NON-NLS-1$
		}
		return style;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options)
	 */
	@Override
	public mxRectangle layoutGraph(GraphOwner owner, Object[] cells,
			Options options) {
		mxGraph graph = owner.getGraph();
		mxIGraphModel model = graph.getModel();
		
		mxRectangle bounds = new mxRectangle();

		model.beginUpdate();
		try {
			for(int i=0; i<cells.length; i++) {
				Object cell = cells[i];
				if(graph.isAutoSizeCell(cell)) {
					graph.cellSizeUpdated(cell, false);
				}
			}
		} finally {
			model.endUpdate();
		}
		
		return bounds;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(de.ims.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], de.ims.icarus.util.Options, com.mxgraph.util.mxRectangle)
	 */
	@Override
	public mxRectangle compressGraph(GraphOwner owner, Object[] cells,
			Options options, mxRectangle bounds) {
		return layoutGraph(owner, cells, options);
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
