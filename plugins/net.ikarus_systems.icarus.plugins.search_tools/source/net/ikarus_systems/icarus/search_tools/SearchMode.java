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
public enum SearchMode {
	
	/**
	 * Every single hit encountered in a target graph
	 * should be cached and the graph reported as a whole.
	 * This effectively implies exhaustive searching!
	 */
	HITS,
	
	/**
	 * Every single hit encountered in a target graph
	 * should be reported independently. This effectively
	 * implies exhaustive searching!
	 */
	INDEPENDENT_HITS,
	
	/**
	 * Only the first hit in a target graph should be reported.
	 * Further processing of that graph is not necessary.
	 */
	MATCH,
}
