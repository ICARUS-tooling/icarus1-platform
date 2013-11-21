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
package de.ims.icarus.language.model;



/**
 * Provides a structural view on a {@link MarkableLayer} by specifying a
 * set of nodes connected by edges.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Structure extends Markable, Iterable<Edge> {
	
	/**
	 * Since a {@code Structure} represents a top-level {@code Markable}
	 * this method must <b>always</b> return {@code null} for any 
	 * {@code Structure} object!
	 * 
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	Container getContainer();

	/**
	 * Returns the container hosting the {@code Markable} objects that serve as
	 * nodes for this structure.
	 * 
	 * @return
	 */
	Container getNodeContainer();
	
	/**
	 * Returns the total number of edges this structure hosts.
	 * @return the total number of edges this structure hosts.
	 */
	int getEdgeCount();
	/**
	 * Returns the {@link Edge} stored at the given position within this
	 * structure.
	 * 
	 * @param index The position of the desired {@code Edge} within this structure
	 * @return The {@code Edge} at position {@code index}
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getEdgeCount()</tt>)
	 */
	Edge getEdgeAt(int index);
	
	/**
	 * Return the number of <b>outgoing</i> edges for a given node.
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor 
	 * {@value StructureType#GRAPH}.
	 * 
	 * @param node the node to query for the number of outgoing edges.
	 * @return the number of <b>outgoing</i> edges for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
	 * @throws IllegalArgumentException if the {@code node} is not a member
	 * of this structure's node-container
	 */
	int getEdgeCount(Markable node);
	
	/**
	 * Return the <b>outgoing</i> edge at position {@code index} for a given node.
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor 
	 * {@value StructureType#GRAPH}.
	 * 
	 * @param node the {@code Markable} in question
	 * @param index the position of the desired {@code Edge} in the list of 
	 * <i>outgoing</i> edges for the given node
	 * @return the <b>outgoing</i> edge at position {@code index} for a given node.
	 * @throws NullPointerException if the {@code node} is {@code null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getEdgeCount(Markable)</tt>)
	 */
	Edge getEdgeAt(Markable node, int index);
	
	/**
	 * Utility method to fetch the <i>parent</i> of a given markable in this
	 * structure. The meaning of the term <i>parent</i> is depending on the
	 * {@code StructureType} as defined in this structure's {@code MarkableLayerManifest}
	 * <p>
	 * This is an optional method and only to be expected when the type of
	 * this structure is neither {@value StructureType#SET} nor 
	 * {@value StructureType#GRAPH}.
	 * 
	 * @param node the node whose parent is to be returned
	 * @return the node's parent or {@code null} if the node has no parent
	 */
	Markable getParent(Markable node);
	
	/**
	 * For non-trivial structures returns the <i>generic root</i> node.
	 * To allow actual root nodes of the structure to contain edge
	 * annotations, they should all be linked to the single
	 * <i>generic root</i> which makes it easier for application code
	 * to collect them in a quick lookup manner.
	 * <p>
	 * What the actual root of a structure is meant to be depends on that
	 * structure's {@code StructureType}:<br>
	 * For a {@value StructureType#CHAIN} this is the first item in the chain,
	 * for a {@value StructureType#TREE} it is the one tree-root. In the case
	 * of general {@value StructureType#GRAPH} structures it will be either a
	 * single node specifically marked as root or each node that has no
	 * incoming edges.
	 * 
	 * @return the <i>generic root</i> of this structure or {@code null} if this
	 * structure is of type {@value StructureType#SET}
	 */
	Markable getRoot();

	/**
	 * Returns the <i>type</i> of this structure. 
	 * @return
	 */
	StructureType getStructureType();
}
