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

import java.text.ParseException;

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
	 * Marks a node that serves as branching point within a disjunction
	 */
	DISJUNCTION("disjunction"), //$NON-NLS-1$
	
	/**
	 * A node that is not a leaf. 
	 */
	INTERMEDIATE("intermediate"); //$NON-NLS-1$
	
	private NodeType(String token) {
		this.token = token;
	}
	
	private String token;
	
	public String getToken() {
		return token;
	}
	
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.nodeType."+token+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.nodeType."+token+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static NodeType parseNodeType(String s) throws ParseException {
		if(s==null || s.isEmpty())
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		
		s = s.toLowerCase();
		
		for(NodeType type : values()) {
			if(type.token.startsWith(s)) {
				return type;
			}
		}
		
		throw new ParseException("Unknown node type string: "+s, 0); //$NON-NLS-1$
	}
}
