/*
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
