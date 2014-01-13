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
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.Structure;
import de.ims.icarus.language.model.util.CorpusUtils;

/**
 * Implements a simple directed edge.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultEdge implements Edge {

	private Markable source, target;
	private final long id;
	private final Structure structure;

	/**
	 * @param structure
	 */
	public DefaultEdge(long id, Structure structure, Markable source, Markable target) {
		if (structure == null)
			throw new NullPointerException("Invalid structure"); //$NON-NLS-1$

		this.id = id;
		this.structure = structure;

		setSource(source);
		setTarget(target);
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
	 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		return Math.min(source.getBeginOffset(), target.getBeginOffset());
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return Math.max(source.getEndOffset(), target.getEndOffset());
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.EDGE;
	}

	/**
	 * @see de.ims.icarus.language.model.Edge#getStructure()
	 */
	@Override
	public Structure getStructure() {
		return structure;
	}

	/**
	 * @see de.ims.icarus.language.model.Edge#getSource()
	 */
	@Override
	public Markable getSource() {
		return source;
	}

	/**
	 * @see de.ims.icarus.language.model.Edge#getTarget()
	 */
	@Override
	public Markable getTarget() {
		return target;
	}

	/**
	 * @see de.ims.icarus.language.model.Edge#isDirected()
	 */
	@Override
	public boolean isDirected() {
		return true;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getContainer()
	 */
	@Override
	public Container getContainer() {
		// We are not hosted within a container, but a structure!
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getLayer()
	 */
	@Override
	public MarkableLayer getLayer() {
		return structure.getLayer();
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getCorpus()
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
		if(obj instanceof Edge) {
			Edge other = (Edge) obj;
			return other.isDirected()==isDirected()
					&& other.getStructure().equals(getStructure())
					&& getSource().equals(other.getSource())
					&& getTarget().equals(other.getTarget());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Edge("+source+","+target+")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}
