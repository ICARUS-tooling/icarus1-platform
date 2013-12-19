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

import java.util.Iterator;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Edge;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.Structure;
import de.ims.icarus.language.model.StructureType;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.standard.LookupList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Graph implements Structure {

	protected LookupList<Edge> edges;
	protected Container nodes;

	protected final Markable root;

	protected int[] links;

	public Graph() {
		root = new Root(this);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return ContainerType.LIST;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getBaseContainer()
	 */
	@Override
	public Container getBaseContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableCount()
	 */
	@Override
	public int getMarkableCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getMarkableAt(int)
	 */
	@Override
	public Markable getMarkableAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#indexOfMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#containsMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public void addMarkable(Markable markable) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable(int, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable removeMarkable(Markable markable) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public void moveMarkable(Markable markable, int index) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getId()
	 */
	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Markable> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#isMultiRoot()
	 */
	@Override
	public boolean isMultiRoot() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#indexOfEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#containsEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeCount(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int getEdgeCount(Markable node) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeAt(de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public Edge getEdgeAt(Markable node, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getParent(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable getParent(Markable node) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getRoot()
	 */
	@Override
	public Markable getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Structure#addEdge(de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable addEdge(Markable source, Markable target) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#addEdge(de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#removeEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Structure#moveEdge(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see de.ims.icarus.language.model.Structure#setTerminal(de.ims.icarus.language.model.Edge, de.ims.icarus.language.model.Markable, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Markable markable, boolean isSource) {
		// TODO Auto-generated method stub

	}


}
