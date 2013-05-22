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
 * 
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SearchGraph {

	SearchNode[] getNodes();
	
	SearchEdge[] getEdges();
	
	SearchNode[] getRootNodes();
}
