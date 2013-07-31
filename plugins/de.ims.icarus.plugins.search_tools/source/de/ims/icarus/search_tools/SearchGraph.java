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
package de.ims.icarus.search_tools;


/**
 * 
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchGraph {
	
	public static final int OPERATOR_CONJUNCTION = 1;
	public static final int OPERATOR_DISJUNCTION = 2;

	/**
	 * Returns the operator to be applied in case that more than
	 * one independent sub-graph is contained within this {@code SearchGraph}.
	 * <p>
	 * Note that in the case of disjunction and groupings in different
	 * sub-graphs a mapping between them is required to aggregate the instances
	 * in the result. 
	 */
	int getRootOperator();
	
	SearchNode[] getNodes();
	
	SearchEdge[] getEdges();
	
	SearchNode[] getRootNodes();
	
	SearchGraph clone();
}
