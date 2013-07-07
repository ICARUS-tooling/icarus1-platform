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

import javax.swing.Icon;

import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum NodeType implements Identity {

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
	
	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.nodeType."+token+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
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

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
