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
package de.ims.icarus.language.model.standard.elements;

import de.ims.icarus.language.model.api.Container;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.Edge;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.Structure;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.language.model.util.Recyclable;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * Implements a simple directed edge.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public class DefaultEdge implements Edge, Recyclable {

	@Reference
	private Markable source;
	@Reference
	private Markable target;
	@Reference(ReferenceType.UPLINK)
	private Structure structure;

	/**
	 * @param structure
	 */
	public DefaultEdge(Structure structure, Markable source, Markable target) {
		if (structure == null)
			throw new NullPointerException("Invalid structure"); //$NON-NLS-1$

		this.structure = structure;

		setSource(source);
		setTarget(target);
	}

	/**
	 * @param structure the structure to set
	 */
	public void setStructure(Structure structure) {
		if (structure == null)
			throw new NullPointerException("Invalid structure"); //$NON-NLS-1$

		this.structure = structure;
	}

	/**
	 * @see de.ims.icarus.language.model.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		source = target = null;
		structure = null;
	}

	/**
	 * @see de.ims.icarus.language.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		return source!=null && target!=null && structure!=null;
	}

	/**
	 * @param source the source to set
	 */
	@Override
	public void setSource(Markable source) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		this.source = source;
	}

	/**
	 * @param target the target to set
	 */
	@Override
	public void setTarget(Markable target) {
		if (target == null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		this.target = target;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		return Math.min(source.getBeginOffset(), target.getBeginOffset());
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		return Math.max(source.getEndOffset(), target.getEndOffset());
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.EDGE;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Edge#getStructure()
	 */
	@Override
	public Structure getStructure() {
		return structure;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Edge#getSource()
	 */
	@Override
	public Markable getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Edge#getTarget()
	 */
	@Override
	public Markable getTarget() {
		return target;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Edge#isDirected()
	 */
	@Override
	public boolean isDirected() {
		return true;
	}

	/**
	 * Returns {@code null}, since edges are markables exclusively owned by structure
	 * objects and therefore never have a host container. They might, however, get
	 * added to foreign containers as regular markables, but note, that this does {@code not}
	 * change ownership!
	 *
	 * @see de.ims.icarus.language.model.api.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		// We are not hosted within a container, but a structure!
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return structure.getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return structure.getCorpus();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		return CorpusUtils.compare(this, o);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return source.hashCode() * target.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return this==obj;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return CorpusUtils.toString(this);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getIndex()
	 */
	@Override
	public long getIndex() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#setIndex(long)
	 */
	@Override
	public void setIndex(long newIndex) {
		throw new UnsupportedOperationException("Edges cannot have index values assigned"); //$NON-NLS-1$
	}

}
