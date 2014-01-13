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

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Edge;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.StructureType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DirectedGraph extends AbstractRootedStructure {

	private GraphStruct struct;

	public DirectedGraph(long id, Container parent, Container boundary, boolean augment,
			boolean boundaryAsBase) {
		super(id, parent, boundary, augment, boundaryAsBase);
	}

	public DirectedGraph(long id, Container parent) {
		super(id, parent);
	}



	/**
	 * Refreshes the internal {@link GraphStruct} object according to the
	 * change. Note that subclasses should always call this method via
	 * {@code super#edgeAdded(Edge, int)} after verifying the given change
	 * to make sure that changes are correctly reflected in the internal
	 * graph structure!
	 *
	 * @see de.ims.icarus.language.model.standard.structure.AbstractRootedStructure#edgeAdded(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	protected void edgeAdded(Edge edge, int index) {
		getStruct().addEdge(edge);
	}

	/**
	 * Refreshes the internal {@link GraphStruct} object according to the
	 * change. Note that subclasses should always call this method via
	 * {@code super#edgeAdded(Edge, int)} after verifying the given change
	 * to make sure that changes are correctly reflected in the internal
	 * graph structure!
	 *
	 * @see de.ims.icarus.language.model.standard.structure.AbstractRootedStructure#edgeRemoved(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	protected void edgeRemoved(Edge edge, int index) {
		getStruct().removeEdge(edge);
	}

	/**
	 * Refreshes the internal {@link GraphStruct} object according to the
	 * change. Note that subclasses should always call this method via
	 * {@code super#edgeAdded(Edge, int)} after verifying the given change
	 * to make sure that changes are correctly reflected in the internal
	 * graph structure!
	 *
	 * @see de.ims.icarus.language.model.standard.structure.AbstractRootedStructure#terminalChanged(de.ims.icarus.language.model.Edge, boolean, de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable)
	 */
	@Override
	protected void terminalChanged(Edge edge, boolean isSource,
			Markable oldTerminal, Markable newTerminal) {
		GraphStruct struct = getStruct();

		struct.removeEdge(!isSource, oldTerminal, edge);
		struct.addEdge(!isSource, newTerminal, edge);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.DIRECTED_GRAPH;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getEdgeCount(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int getEdgeCount(Markable node) {
		return getStruct().getEdgeCount(false, node);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getEdgeAt(de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public Edge getEdgeAt(Markable node, int index) {
		return getStruct().getEdgeAt(false, node, index);
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#getParent(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable getParent(Markable node) {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.structure.EmptyStructure#invalidate()
	 */
	@Override
	protected void invalidate() {
		deleteStruct();
	}

	protected void deleteStruct() {
		struct = null;
	}

	protected GraphStruct getStruct() {
		if(struct==null) {
			struct = new GraphStruct();
			buildStruct(struct);
		}

		return struct;
	}

	protected void buildStruct(GraphStruct struct) {

		for(int i=0; i<getEdgeCount(); i++) {
			struct.addEdge(getEdgeAt(i));
		}
	}
}
