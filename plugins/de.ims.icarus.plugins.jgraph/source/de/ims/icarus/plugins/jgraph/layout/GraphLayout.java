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
