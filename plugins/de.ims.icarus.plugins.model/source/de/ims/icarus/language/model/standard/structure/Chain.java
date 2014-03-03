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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.standard.structure;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.StructureType;


/**
 * Implements a simple chain structure with the following constraints:
 * <ul>
 * <li>there may be multiple independent root nodes</li>
 * <li>all edges are directed</li>
 * <li>each node has exactly one <i>parent</i> (either another non-root node
 * or the virtual ROOT markable obtainable via {@link #getRoot()})</li>
 * <li>each node can have at most one child node (except for the virtual
 * ROOT, which can have an arbitrary number of children)</li>
 * </ul>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Chain extends Tree {

	public Chain(long id, Container parent, Container boundary, boolean augment,
			boolean boundaryAsBase) {
		super(id, parent, boundary, augment, boundaryAsBase);
	}

	public Chain(long id, Container parent) {
		super(id, parent);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.DirectedGraph#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.CHAIN;
	}

	@Override
	protected void checkNodeConstraints(Markable node) {
		if(getStruct().getEdgeCount(true, node)>0)
			throw new IllegalArgumentException("Node already has a parent: "+node); //$NON-NLS-1$
		if(!isRoot(node) && getStruct().getEdgeCount(false, node)>0)
			throw new IllegalArgumentException("Node already has a successor: "+node); //$NON-NLS-1$
	}
}
