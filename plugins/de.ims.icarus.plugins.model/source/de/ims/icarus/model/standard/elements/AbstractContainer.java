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
package de.ims.icarus.model.standard.elements;

import de.ims.icarus.model.api.edit.EditOperation;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ItemLayerManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Link;
import de.ims.icarus.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class AbstractContainer extends AbstractItem implements Container {

	@Reference
	private Container boundary;
	@Link
	private MemberSet<Container> base = EMPTY_BASE_SET;

	/**
	 * @param id
	 * @param container
	 */
	protected AbstractContainer() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.model.util.Recyclable#recycle()
	 */
	@Override
	public void recycle() {
		super.recycle();
		boundary = null;
		base = null;
	}

	/**
	 * @see de.ims.icarus.model.util.Recyclable#revive()
	 */
	@Override
	public boolean revive() {
		if(!super.revive()) {
			return false;
		}

//		ContainerManifest manifest = getManifest();

		//FIXME maybe add boundary and base container manifests again?

//		if(manifest.getParentManifest()!=null && getContainer()==null) {
//			return false;
//		}

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
	 * @see de.ims.icarus.model.api.members.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.CONTAINER;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return getManifest().getContainerType();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getBeginOffset()
	 */
	@Override
	public long getBeginOffset() {
		if(getMarkableCount()==0)
			throw new IllegalStateException("Container is empty"); //$NON-NLS-1$

		return getItemAt(0).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Item#getEndOffset()
	 */
	@Override
	public long getEndOffset() {
		if(getMarkableCount()==0)
			throw new IllegalStateException("Container is empty"); //$NON-NLS-1$

		return getItemAt(getMarkableCount()-1).getEndOffset();
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#containsItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public boolean containsItem(Item item) {
		return indexOfItem(item)!=-1;
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
	 * @see de.ims.icarus.model.api.members.Container#getBaseContainer()
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
	 * @see de.ims.icarus.model.api.members.Structure#getBoundaryContainer()
	 */
	@Override
	public Container getBoundaryContainer() {
		return boundary;
	}

	/**
	 * To decrease memory footprint this implementation does not
	 * store a reference to the assigned manifest itself, but rather
	 * checks the depth of nesting and forwards the call to the
	 * {@link ItemLayerManifest} that describes this
	 * container's root.
	 *
	 * @see de.ims.icarus.model.api.members.Container#getManifest()
	 */
	@Override
	public ContainerManifest getManifest() {
		return CorpusUtils.getContainerManifest(this);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#indexOfItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public int indexOfItem(Item item) {
		if (item == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		int size = getMarkableCount();

		for(int i=0; i<size; i++) {
			if(item.equals(getItemAt(i))) {
				return i;
			}
		}

		return -1;
	}

	// Shorthand edit methods

	/**
	 * @see de.ims.icarus.model.api.members.Container#addMarkable()
	 */
	@Override
	public void addItem(Item item) {
		addItem(getMarkableCount(), item);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeItem(de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public Item removeItem(Item item) {
		return removeItem(indexOfItem(item));
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#moveItem(de.ims.icarus.model.api.members.Item, int)
	 */
	@Override
	public void moveItem(Item item, int index) {
		moveMarkable(indexOfItem(item), index);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeAllMarkables()
	 */
	@Override
	public void removeAllMarkables() {
		checkContainerAction(EditOperation.CLEAR);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#addItem(int, de.ims.icarus.model.api.members.Item)
	 */
	@Override
	public void addItem(int index, Item item) {
		checkContainerAction(EditOperation.ADD_RANDOM);
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#removeItem(int)
	 */
	@Override
	public Item removeItem(int index) {
		checkContainerAction(EditOperation.REMOVE_RANDOM);
		return null;
	}

	/**
	 * @see de.ims.icarus.model.api.members.Container#moveMarkable(int, int)
	 */
	@Override
	public void moveMarkable(int index0, int index1) {
		checkContainerAction(EditOperation.MOVE);
	}

	protected void checkItem(Item item) {
		if (item == null)
			throw new NullPointerException("Invalid markable"); //$NON-NLS-1$

		if(boundary!=null && !CorpusUtils.isVirtual(item)
				&& !CorpusUtils.contains(boundary, item))
			throw new IllegalArgumentException("Item violates boundary"); //$NON-NLS-1$

		Container container = item.getContainer();

		if(container!=this && !getBaseContainers().contains(container))
			throw new IllegalArgumentException("Item's host container is unknown: "+container); //$NON-NLS-1$
	}
}
