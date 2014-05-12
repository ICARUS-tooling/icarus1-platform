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
import de.ims.icarus.language.model.api.ContainerType;
import de.ims.icarus.language.model.api.Markable;
import de.ims.icarus.language.model.api.MemberSet;
import de.ims.icarus.language.model.api.MemberType;
import de.ims.icarus.language.model.api.edit.EditOperation;
import de.ims.icarus.language.model.api.manifest.ContainerManifest;
import de.ims.icarus.language.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class AbstractContainer extends AbstractMarkable implements Container {

	@Reference
	private Container boundary;
	@Reference
	private MemberSet<Container> base;

	/**
	 * @param id
	 * @param container
	 */
	protected AbstractContainer() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.language.model.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		boundary = null;
		base = null;
	}

	/**
	 * @see de.ims.icarus.language.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		if(!super.revive()) {
			return false;
		}

		ContainerManifest manifest = getManifest();

		if(manifest.getBaseContainerManifest()!=null && base!=null) {
			return false;
		}

		if(manifest.getParentManifest()!=null && getContainer()==null) {
			return false;
		}

		if(manifest.getBoundaryContainerManifest()!=null && boundary==null) {
			return false;
		}

		return true;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) (getBeginOffset()*getEndOffset()+1);
	}

	public Container getPrimaryBaseContainer() {
		return getBaseContainers().elementAt(0);
	}

	protected void checkContainerAction(EditOperation operation) {
		ContainerType type = getContainerType();
		if(!type.supportsOperation(operation))
			throw new UnsupportedOperationException(operation.name()+" not supported on container-type "+type.name()); //$NON-NLS-1$
	}

	protected boolean isRandomAccessMarkableIndex(int index) {
		return index>0 && index<getMarkableCount()-1;
	}

	/**
	 * @see de.ims.icarus.language.model.api.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.CONTAINER;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return getManifest().getContainerType();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		if(getMarkableCount()==0)
			throw new IllegalStateException("Container is empty"); //$NON-NLS-1$

		return getMarkableAt(0).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Markable#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		if(getMarkableCount()==0)
			throw new IllegalStateException("Container is empty"); //$NON-NLS-1$

		return getMarkableAt(getMarkableCount()-1).getEndOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#containsMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		return indexOfMarkable(markable)!=-1;
	}

	/**
	 * @param base the base to set
	 */
	public void setBaseContainers(MemberSet<Container> base) {
		if(base==null)
			throw new NullPointerException("Invalid base"); //$NON-NLS-1$
		this.base = base;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#getBaseContainer()
	 */
	@Override
	public MemberSet<Container> getBaseContainers() {
		return base;
	}

	/**
	 * @param boundary the boundary to set
	 */
	public void setBoundaryContainer(Container boundary) {
		if(boundary==null)
			throw new NullPointerException("Invalid boundary"); //$NON-NLS-1$
		this.boundary = boundary;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Structure#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return boundary;
	}

	/**
	 * To decrease memory footprint this implementation does not
	 * store a reference to the assigned manifest itself, but rather
	 * checks the depth of nesting and forwards the call to the
	 * {@link MarkableLayerManifest} that describes this
	 * container's root.
	 *
	 * @see de.ims.icarus.language.model.api.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return CorpusUtils.getContainerManifest(this);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#indexOfMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		int size = getMarkableCount();

		for(int i=0; i<size; i++) {
			if(markable.equals(getMarkableAt(i))) {
				return i;
			}
		}

		return -1;
	}

	// Shorthand edit methods

	/**
	 * @see de.ims.icarus.language.model.api.Container#addMarkable()
	 */
	@Override
	public void addMarkable(Markable markable) {
		addMarkable(getMarkableCount(), markable);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeMarkable(de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public Markable removeMarkable(Markable markable) {
		return removeMarkable(indexOfMarkable(markable));
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#moveMarkable(de.ims.icarus.language.model.api.Markable, int)
	 */
	@Override
	public void moveMarkable(Markable markable, int index) {
		moveMarkable(indexOfMarkable(markable), index);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		checkContainerAction(EditOperation.CLEAR);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#addMarkable(int, de.ims.icarus.language.model.api.Markable)
	 */
	@Override
	public void addMarkable(int index, Markable markable) {
		checkContainerAction(EditOperation.ADD_RANDOM);
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#removeMarkable(int)
	 */
	@Override
	public Markable removeMarkable(int index) {
		checkContainerAction(EditOperation.REMOVE_RANDOM);
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		checkContainerAction(EditOperation.MOVE);
	}

	protected void checkMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		if(boundary!=null && !CorpusUtils.isVirtual(markable)
				&& !CorpusUtils.contains(boundary, markable))
			throw new IllegalArgumentException("Markable violates boundary"); //$NON-NLS-1$

		Container container = markable.getContainer();

		if(container!=this && !getBaseContainers().contains(container))
			throw new IllegalArgumentException("Markable's host container is unknown: "+container); //$NON-NLS-1$
	}
}
