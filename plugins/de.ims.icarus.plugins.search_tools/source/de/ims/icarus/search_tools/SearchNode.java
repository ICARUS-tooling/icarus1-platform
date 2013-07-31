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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchNode {
	
	String getId();

	SearchConstraint[] getConstraints();
	
	boolean isNegated();
	
	int getOutgoingEdgeCount();
	
	SearchEdge getOutgoingEdgeAt(int index);
	
	int getIncomingEdgeCount();
	
	SearchEdge getIncomingEdgeAt(int index);
	
	NodeType getNodeType();
	
	/**
	 * Returns the height of the sub-tree whose root node is this {@code SearchNode}. 
	 * For a leaf node this method must return {@code 1} and for any
	 * other node it is {@code 1} plus the maximum of any of its child nodes height. 
	 */
	int getHeight();
	
	/**
	 * Returns the number of nodes in the sub-tree whose root this {@code SearchNode} is. 
	 * This count does not include the node itself and is {@code 0} for leaf nodes.
	 */
	int getDescendantCount();
	
	/**
	 * Returns the minimum number of direct child nodes.
	 */
	int getChildCount();
}
