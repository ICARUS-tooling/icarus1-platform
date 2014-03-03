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
import de.ims.icarus.language.model.api.Edge;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.StructureType;

/**
 * Implements a simple tree structure with the following constraints:
 * <ul>
 * <li>there may be multiple independent root nodes</li>
 * <li>all edges are directed</li>
 * <li>each node has exactly one <i>parent</i> (either another non-root node
 * or the virtual ROOT markable obtainable via {@link #getRoot()})</li>
 * <li>each node can have an arbitrary number of child nodes</li>
 * </ul>
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Tree extends DirectedGraph {

	public Tree(long id, Container parent, Container boundary, boolean augment,
			boolean boundaryAsBase) {
		super(id, parent, boundary, augment, boundaryAsBase);
	}

	public Tree(long id, Container parent) {
		super(id, parent);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.DirectedGraph#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.TREE;
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.DirectedGraph#edgeAdded(de.ims.icarus.language.model.api.Edge, int)
	 */
	@Override
	protected void edgeAdded(Edge edge, int index) {
		checkNodeConstraints(edge.getTarget());

		super.edgeAdded(edge, index);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.DirectedGraph#terminalChanged(de.ims.icarus.language.model.api.Edge, boolean, de.ims.icarus.language.model.api.Markable, de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	protected void terminalChanged(Edge edge, boolean isSource,
			Markable oldTerminal, Markable newTerminal) {
		checkNodeConstraints(newTerminal);

		super.terminalChanged(edge, isSource, oldTerminal, newTerminal);
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.DirectedGraph#getParent(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public Markable getParent(Markable node) {
		GraphStruct struct = getStruct();

		if(struct.getEdgeCount(true, node)==0) {
			return null;
		}

		Edge edge = struct.getEdgeAt(true, node, 0);

		return edge.getSource();
	}

	/**
	 * @see de.ims.icarus.language.model.api.standard.structure.AbstractRootedStructure#checkEdge(de.ims.icarus.language.model.api.Edge)
	 */
	@Override
	protected void checkEdge(Edge edge) {
		super.checkEdge(edge);

		checkNodeConstraints(edge.getTarget());
	}

	protected void checkNodeConstraints(Markable node) {
		if(getStruct().getEdgeCount(true, node)>0)
			throw new IllegalArgumentException("Node already has a parent: "+node); //$NON-NLS-1$
	}
}
