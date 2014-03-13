/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision: 159 $
 * $Date: 2013-11-04 11:20:05 +0100 (Mo, 04 Nov 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.search_tools/source/de/ims/icarus/search_tools/EdgeType.java $
 *
 * $LastChangedDate: 2013-11-04 11:20:05 +0100 (Mo, 04 Nov 2013) $ 
 * $LastChangedRevision: 159 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.search;

import java.text.ParseException;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id: EdgeType.java 159 2013-11-04 10:20:05Z mcgaerty $
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
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$
		
		s = s.toLowerCase();
		
		for(EdgeType type : values()) {
			if(type.token.startsWith(s)) {
				return type;
			}
		}
		
		throw new ParseException("Unknown edge type string: "+s, 0); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getId()
	 */
	@Override
	public String getId() {
		return getClass().getSimpleName();
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus.util.id.Identity#getOwner()
	 */
	@Override
	public Object getOwner() {
		return this;
	}
}
