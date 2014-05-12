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
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.iql.elements;

import java.text.ParseException;

import javax.swing.Icon;

import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.util.id.Identity;


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
			throw new NullPointerException("Invalid string"); //$NON-NLS-1$
		
		s = s.toLowerCase();
		
		for(NodeType type : values()) {
			if(type.token.startsWith(s)) {
				return type;
			}
		}
		
		throw new ParseException("Unknown node type string: "+s, 0); //$NON-NLS-1$
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
