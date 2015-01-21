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
package de.ims.icarus.model.api.members;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.model.api.edit.EditOperation;
import de.ims.icarus.model.xml.XmlResource;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum StructureType implements XmlResource {

	/**
	 * An unordered collection of nodes, not connected
	 * by any edges. This is by far the most basic type of
	 * structure.
	 *
	 * @deprecated no need to support empty structures
	 */
	@Deprecated
	SET("set", false, 0, 0, 0), //$NON-NLS-1$

	/**
	 * An ordered sequence of nodes, each with at most one
	 * predecessor and successor. Edges in this structure are
	 * expected to be {@code directed} only!
	 */
	CHAIN("chain", true, 1, 1, 1), //$NON-NLS-1$

	/**
	 * A hierarchically ordered collection of nodes where each node
	 * is assigned at most one parent and is allowed to have an arbitrary
	 * number of children. All edges are {@code directed} from a parent
	 * down to the child node itself.
	 */
	TREE("tree", true, -1, 1, 1), //$NON-NLS-1$

	/**
	 * A general graph with the only restriction that edges have to be
	 * directed.
	 */
	DIRECTED_GRAPH("directed-graph", true, -1, -1, 0), //$NON-NLS-1$

	/**
	 * Being the most unbounded and therefore most complex type a {@code GRAPH}
	 * does not pose any restrictions on nodes or edges.
	 */
	GRAPH("graph", false, -1, -1, 0); //$NON-NLS-1$

	private final int outgoingEdgeLimit;
	private final int incomingEdgeLimit;
	private final int minEdgeCount;
	private final boolean directed;
	private final EnumSet<EditOperation> operations;
	private final String xmlForm;

	private StructureType(String xmlForm, boolean directed, int outgoingEdgeLimit,
			int incomingEdgeLimit, int minEdgeCount,
			EditOperation...operations) {

		this.xmlForm = xmlForm;
		this.outgoingEdgeLimit = outgoingEdgeLimit;
		this.incomingEdgeLimit = incomingEdgeLimit;
		this.minEdgeCount = minEdgeCount;
		this.directed = directed;

		if(operations==null || operations.length==0) {
			this.operations = EnumSet.allOf(EditOperation.class);
		} else {
			this.operations = EnumSet.noneOf(EditOperation.class);
			for(EditOperation operation : operations) {
				this.operations.add(operation);
			}
		}
	}

	/**
	 * @return the operations
	 */
	public EditOperation[] getOperations() {
		return operations.toArray(new EditOperation[operations.size()]);
	}

	/**
	 * Returns whether or not the given operation is supported on this
	 * type of structure.
	 * @param operation The operation in question
	 * @return {@code true} iff the given operation is supported on this
	 * structure type
	 * @throws NullPointerException if the {@code operation} argument
	 * is {@code null}
	 */
	public boolean supportsOperation(EditOperation operation) {
		if (operation == null)
			throw new NullPointerException("Invalid operation");  //$NON-NLS-1$

		return operations.contains(operation);
	}

	/**
	 * Returns the maximum number of child nodes a single node is
	 * allows to have in this structure type.
	 * <p>
	 * Note that this does <b>not</b> affect the root node!
	 * <p>
	 * A value of {@code -1} indicated no limit.
	 *
	 * @return the outgoingEdgeLimit
	 */
	public int getOutgoingEdgeLimit() {
		return outgoingEdgeLimit;
	}

	public boolean isLegalOutgoingEdgeCount(int count) {
		return outgoingEdgeLimit!=-1 && count<=outgoingEdgeLimit;
	}

	/**
	 * Returns the maximum number of parent nodes a single node is
	 * allows to have in this structure type.
	 * <p>
	 * Note that this does <b>not</b> affect the root node!
	 * <p>
	 * A value of {@code -1} indicated no limit.
	 *
	 * @return the incomingEdgeLimit
	 */
	public int getIncomingEdgeLimit() {
		return incomingEdgeLimit;
	}

	public boolean isLegalIncomingEdgeCount(int count) {
		return incomingEdgeLimit!=-1 && count<=incomingEdgeLimit;
	}

	/**
	 * Returns the minimum number of edges a single node must
	 * have assigned.
	 * <p>
	 * Note that this does <b>not</b> affect the root node!
	 * <p>
	 * A value of {@code -1} indicated no limit.
	 *
	 * @return the minEdgeCount
	 */
	public int getMinEdgeCount() {
		return minEdgeCount;
	}

	public boolean isLegalEdgeCount(int count) {
		return minEdgeCount!=-1 && count>=minEdgeCount;
	}

	/**
	 * @return the directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @see de.ims.icarus.model.api.xml.XmlResource#getXmlValue()
	 */
	@Override
	public String getXmlValue() {
		return xmlForm;
	}

	private static Map<String, StructureType> xmlLookup;

	public static StructureType parseStructureType(String s) {
		if(xmlLookup==null) {
			Map<String, StructureType> map = new HashMap<>();
			for(StructureType type : values()) {
				map.put(type.xmlForm, type);
			}
			xmlLookup = map;
		}

		return xmlLookup.get(s);
	}
}
