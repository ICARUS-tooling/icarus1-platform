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
public class BasicTreeLayout implements GraphLayout {

	public BasicTreeLayout() {
		// no-op
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
