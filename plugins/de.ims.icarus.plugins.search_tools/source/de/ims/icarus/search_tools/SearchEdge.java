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
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchEdge {
	
	String getId();

	SearchConstraint[] getConstraints();
	
	boolean isNegated();
	
	EdgeType getEdgeType();
	
	SearchNode getSource();
	
	SearchNode getTarget();
}
