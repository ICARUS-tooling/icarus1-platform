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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.model;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum StructureType {

	/**
	 * An unordered collection of nodes, not connected
	 * by any edges. This is by far the most basic type of
	 * structure.
	 */
	SET,
	
	/**
	 * An ordered sequence of nodes, each with at most one
	 * predecessor and successor. Edges in this structure are
	 * expected to be {@code directed} only!
	 */
	CHAIN,
	
	/**
	 * A hierarchically ordered collection of nodes where each node
	 * is assigned at most one parent and is allowed to have an arbitrary
	 * number of children. All edges are {@code directed} from a parent
	 * down to the child node itself.
	 */
	TREE,
	
	/**
	 * Being the most unbounded and therefore most complex type a {@code GRAPH}
	 * does not pose any restrictions on nodes or edges.
	 */
	GRAPH;
}
