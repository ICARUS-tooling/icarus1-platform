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
public enum NodeType {

	/**
	 * Node without incoming edges and at least one outgoing edge
	 */
	ROOT("root"), //$NON-NLS-1$
	
	/**
	 * Node without outgoing edges and exactly one incoming edge
	 */
	LEAF("leaf"), //$NON-NLS-1$
	
	/**
	 * A node without restrictions
	 */
	GENERAL("general"), //$NON-NLS-1$
	
	/**
	 * Marks a node that serves as branching point within a dusjunction
	 */
	DISJUNCTION("disjunction"), //$NON-NLS-1$
	
	/**
	 * A node that is not a leaf. 
	 */
	INTERMEDIATE("intermediate"); //$NON-NLS-1$
	
	private NodeType(String key) {
		this.key = key;
	}
	
	private String key;
	
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.nodeType."+key+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.nodeType."+key+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
