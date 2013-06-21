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
public enum EdgeType implements Identity {

	/**
	 * Marks the edge as an empty connection that only serves
	 * as a link between two nodes and does not carry any
	 * constraints.
	 */
	LINK("link"), //$NON-NLS-1$

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
	
	private EdgeType(String token) {
		this.token = token;
	}
	
	private String token;
	
	public String getToken() {
		return token;
	}

	@Override
	public String getName() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.edgeType."+token+".name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDescription() {
		return ResourceManager.getInstance().get(
				"plugins.searchTools.edgeType."+token+".description"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public static EdgeType parseEdgeType(String s) throws ParseException {
		if(s==null || s.isEmpty())
			throw new IllegalArgumentException("Invalid string"); //$NON-NLS-1$
		
		s = s.toLowerCase();
		
		for(EdgeType type : values()) {
			if(type.token.startsWith(s)) {
				return type;
			}
		}
		
		throw new ParseException("Unknown edge type string: "+s, 0); //$NON-NLS-1$
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
