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


import com.mxgraph.util.mxRectangle;

import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GraphLayout extends Installable, GraphLayoutConstants {
	
	/**
	 * Special method to handle new edges in a graph.
	 * @return 
	 */
	String getEdgeStyle(GraphOwner owner, Object edge, Options options);
	
	/**
	 * Layout the given collection of cells in the graph.
	 * If relying on a specific order of cells to arrange them
	 * the order given by the {@code cells} array can be used.
	 */
	mxRectangle layoutGraph(GraphOwner owner, Object[] cells, Options options);
	
	/**
	 * Layout the given collection of cells in the graph so
	 * that it fits within the {@code bounds} rectangle. If
	 * such compression is not possible then a <i>best-effort</i>
	 * approach should be used to reduce the overhead in size as
	 * much as possible.
	 * 
	 * @see GraphLayoutConstants#CELL_FILTER_KEY
	 * @see GraphLayoutConstants#CELL_MERGER_KEY
	 */
	mxRectangle compressGraph(GraphOwner owner, Object[] cells, Options options, mxRectangle bounds);
	
	Object getSignificantCell(GraphOwner owner, Object[] cells, Options options);
}
