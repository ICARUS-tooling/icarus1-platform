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

import net.ikarus_systems.icarus.resources.ResourceManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum EdgeType {

	/**
	 * Marks the edge as active part of some functional relation.
	 * Enables all kinds of constraints on the edge to be taken
	 * into account when used in a matching process.
	 */
	DOMINANCE("dominance"), //$NON-NLS-1$
	
	/**
	 * Transitive closure. Assigned edge matches any directed path
	 * between two nodes that are matched by the source and target
	 * {@code SearchNode} of this edge. Constraints set for this edge
	 * might be used for the initial matching check but subsequent
	 * expansions should ignore them.
	 * <p>
	 * This edge type cannot be used together with negation!
	 */
	TRANSITIVE("transitive"), //$NON-NLS-1$
	
	/**
	 * Matching against total order of source and target {@code SearchNode}.
	 * Note that the concrete order is data specific and may vary. Usually
	 * it is given by the initial order of word tokens within a sentence
	 * that are represented by nodes in the graph.
	 */
	PRECEDENCE("precedence"); //$NON-NLS-1$
	
	private EdgeType(String key) {
		this.key = key;
	}
	
	private String key;
	
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.edgeType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.edgeType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
