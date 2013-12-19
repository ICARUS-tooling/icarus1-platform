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
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Edge;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MarkableLayer;
import de.ims.icarus.language.model.Structure;
import de.ims.icarus.language.model.StructureType;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.manifest.StructureManifest;
import de.ims.icarus.language.model.standard.container.AbstractListContainer;
import de.ims.icarus.language.model.util.CorpusUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EmptyStructure extends AbstractListContainer implements Structure {

	private final Container boundary;
	private final Container parent;

	public EmptyStructure(Container parent) {
		this(parent, null);
	}

	public EmptyStructure(Container parent, Container boundary) {
		if (parent == null)
			throw new NullPointerException("Invalid parent"); //$NON-NLS-1$

		this.parent = parent;
		this.boundary = boundary;
	}

	/**
	 * Checks whether or not the given markable is allowed to be added
	 * to this structure. If either the <i>boundary-container</i> is {@code null}
	 * or the given {@code markable} is virtual as determined by
	 * {@link CorpusUtils#isVirtual(Markable)} then this method simply returns.
	 * Otherwise it compares the begin and end offset of the markable with those
	 * of the <i>boundary-container</i>. If one of those indices lays outside the
	 * boundary it will throw an {@code IllegalArgumentException}.
	 *
	 * @param markable
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code markable} violates the boundary
	 */
	protected void checkMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		if(boundary==null || CorpusUtils.isVirtual(markable)) {
			return;
		}

		if(markable.getBeginOffset()<boundary.getBeginOffset()
				|| markable.getEndOffset()>boundary.getEndOffset())
			throw new IllegalArgumentException("Markable not within boundary: "+markable); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getManifest()
	 */
	@Override
	public StructureManifest getManifest() {
		// Fetch the container level and ask the
		// hosting markable layer manifest for the container
		// manifest at the specific level

		// We assume that this container is nested at least one level
		// below a root container
		int level = 2;

		Container parent = getContainer();
		while(parent.getContainer()!=null) {
			level++;
			parent = parent.getContainer();
		}

		MarkableLayerManifest manifest = getLayer().getManifest();

		return (StructureManifest) manifest.getContainerManifest(level);
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		return parent;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return parent.getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return parent.getCorpus();
	}

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable(int, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		checkMarkable(markable);
		super.addMarkable(index, markable);
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getStructureType()
	 */
	@Override
	public StructureType getStructureType() {
		return StructureType.SET;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#isMultiRoot()
	 */
	@Override
	public boolean isMultiRoot() {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return boundary;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeCount()
	 */
	@Override
	public int getEdgeCount() {
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeAt(int)
	 */
	@Override
	public Edge getEdgeAt(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#indexOfEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public int indexOfEdge(Edge edge) {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#containsEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public boolean containsEdge(Edge edge) {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeCount(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int getEdgeCount(Markable node) {
		return 0;
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getEdgeAt(de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public Edge getEdgeAt(Markable node, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getParent(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable getParent(Markable node) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#getRoot()
	 */
	@Override
	public Markable getRoot() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#removeAllEdges()
	 */
	@Override
	public void removeAllEdges() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#addEdge(de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable addEdge(Markable source, Markable target) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#addEdge(de.ims.icarus.language.model.Markable, de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public Edge addEdge(Markable source, Markable target, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#removeEdge(int)
	 */
	@Override
	public Edge removeEdge(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#removeEdge(de.ims.icarus.language.model.Edge)
	 */
	@Override
	public Edge removeEdge(Edge edge) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#moveEdge(int, int)
	 */
	@Override
	public void moveEdge(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#moveEdge(de.ims.icarus.language.model.Edge, int)
	 */
	@Override
	public void moveEdge(Edge edge, int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.model.Structure#setTerminal(de.ims.icarus.language.model.Edge, de.ims.icarus.language.model.Markable, boolean)
	 */
	@Override
	public void setTerminal(Edge edge, Markable markable, boolean isSource) {
		throw new UnsupportedOperationException();
	}

}
