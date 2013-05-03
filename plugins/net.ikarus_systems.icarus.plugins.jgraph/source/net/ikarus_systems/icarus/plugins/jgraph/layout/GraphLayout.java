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
public interface GraphLayout {
	
	/**
	 * Layout the given collection of cells in the graph.
	 * If relying on a specific order of cells to arrange them
	 * the order given by the {@code cells} array can be used.
	 */
	void layoutGraph(GraphOwner owner, Object[] cells, Options options);
	
	/**
	 * Layout the given collection of cells in the graph so
	 * that it fits within the {@code bounds} rectangle. If
	 * such compression is not possible then a <i>best-effort</i>
	 * approach should be used to reduce the overhead in size as
	 * much as possible. The {@code filter} argument is used to identify
	 * cells that must not be merged or otherwise made non-visible
	 * during compression at all costs!
	 */
	void compressGraph(GraphOwner owner, Object[] cells, Options options, Filter filter, mxRectangle bounds);
}
