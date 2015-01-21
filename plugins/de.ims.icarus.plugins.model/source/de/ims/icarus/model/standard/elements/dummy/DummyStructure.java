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
package de.ims.icarus.model.standard.elements.dummy;

import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.api.members.Edge;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.Structure;
import de.ims.icarus.model.api.members.StructureType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DummyStructure extends DummyContainer implements Structure {

	/**
	 * @see de.ims.icarus.model.standard.elements.dummy.DummyContainer#getManifest()
	 */
	@Override
	public StructureManifest getManifest() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.CHAIN;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#indexOfEdge(de.ims.icarus.model.api.members.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#containsEdge(de.ims.icarus.model.api.members.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getEdgeCount(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public int getEdgeCount(Item node) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getEdgeCount(de.ims.icarus.model.api.members.Item, boolean)
	 */
	@Override
	public int getEdgeCount(Item node, boolean isSource) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getEdgeAt(de.ims.icarus.model.api.members.Item, int, boolean)
	 */
	@Override
	public Edge getEdgeAt(Item node, int index, boolean isSource) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getParent(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public Item getParent(Item node) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#getRoot()
	 */
	@Override
	public Item getRoot() {
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#isRoot(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public boolean isRoot(Item node) {
		return false;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#addEdge(de.ims.icarus.model.api.members.Edge)
	 */
	@Override
	public Edge addEdge(Edge edge) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#addEdge(de.ims.icarus.model.api.members.Edge, int)
	 */
	@Override
	public Edge addEdge(Edge edge, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#addEdge(de.ims.icarus.model.api.members.Item, de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public Edge addEdge(Item source, Item target) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#addEdge(de.ims.icarus.model.api.members.Item, de.ims.icarus.model.api.members.Item, int)
	 */
	@Override
	public Edge addEdge(Item source, Item target, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#removeEdge(de.ims.icarus.model.api.members.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#moveEdge(de.ims.icarus.model.api.members.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Structure#setTerminal(de.ims.icarus.model.api.members.Edge, de.ims.icarus.model.api.members.Item, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Item item, boolean isSource) {
		throw new UnsupportedOperationException();
	}

}
