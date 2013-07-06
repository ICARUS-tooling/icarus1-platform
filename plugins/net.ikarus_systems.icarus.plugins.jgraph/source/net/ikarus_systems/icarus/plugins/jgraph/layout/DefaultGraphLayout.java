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

import net.ikarus_systems.icarus.util.Options;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

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
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(Object target) {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(Object target) {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#getEdgeStyle(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object, net.ikarus_systems.icarus.util.Options)
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
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], net.ikarus_systems.icarus.util.Options)
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
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], net.ikarus_systems.icarus.util.Options, com.mxgraph.util.mxRectangle)
	 */
	@Override
	public mxRectangle compressGraph(GraphOwner owner, Object[] cells,
			Options options, mxRectangle bounds) {
		return layoutGraph(owner, cells, options);
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#getSignificantCell(net.ikarus_systems.icarus.plugins.jgraph.layout.GraphOwner, java.lang.Object[], net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public Object getSignificantCell(GraphOwner owner, Object[] cells,
			Options options) {
		return cells==null || cells.length==0 ? null : cells[0];
	}

}
