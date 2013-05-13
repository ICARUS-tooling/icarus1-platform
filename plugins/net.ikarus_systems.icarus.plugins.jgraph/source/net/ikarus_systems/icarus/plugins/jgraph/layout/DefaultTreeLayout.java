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

import net.ikarus_systems.icarus.util.Filter;
import net.ikarus_systems.icarus.util.Options;

import com.mxgraph.util.mxRectangle;

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
		orderEdgeStyle = ";exitY=1.0;entryY=1.0;exitX=0.5;entryX=0.5;edgeStyle=bottomArcEdgeStyle;shape=arc"; //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#layoutGraph(com.mxgraph.view.mxGraph, java.lang.Object[])
	 */
	@Override
	public void layoutGraph(GraphOwner owner, Object[] cells, Options options) {
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.jgraph.layout.GraphLayout#compressGraph(com.mxgraph.view.mxGraph, java.lang.Object[], com.mxgraph.util.mxRectangle)
	 */
	@Override
	public void compressGraph(GraphOwner owner, Object[] cells, Options options, Filter filter, mxRectangle bounds) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#install(java.lang.Object)
	 */
	@Override
	public void install(GraphOwner target) {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.Installable#uninstall(java.lang.Object)
	 */
	@Override
	public void uninstall(GraphOwner target) {
		// no-op
	}
}
