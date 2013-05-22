/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;

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
	
	/**
	 * Shorthand method for leaf check. Result has to be equivalent
	 * to {@link #getOutgoingEdgeCount()}==0.
	 */
	boolean isLeafNode();
	
	/**
	 * Shorthand method for leaf check. Result has to be equivalent
	 * to {@link #getIncomingEdgeCount()}==0.
	 */
	boolean isRootNode();
	
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
}
