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

import java.util.Arrays;
import java.util.List;

import de.ims.icarus.model.api.members.MemberSet;
import de.ims.icarus.model.util.Recyclable;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.collections.LookupList;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Reference;
import de.ims.icarus.util.mem.ReferenceType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MemberSets {

	@SuppressWarnings("rawtypes")
	public static final MemberSet EMPTY_SET = new EmptySet<>();

	@SuppressWarnings("unchecked")
	public static <E> MemberSet<E> emptySet() {
		return (MemberSet<E>) EMPTY_SET;
	}

	public static final int ARRAY_SET_THRESHOLD = 7;

	public static <E extends Object> MemberSet<E> createMemberSet(List<E> items) {
		if (items == null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$

		int size = items.size();

		if(size==0) {
			return null;
		} else if(size==1) {
			return new SingletonSet<>(items.get(0));
		} else if(size<=ARRAY_SET_THRESHOLD) {
			return new ArraySet<>(items);
		} else {
			return new CachedSet<>(items);
		}
	}

	@HeapMember
	public abstract static class AbstractMemberSet<E extends Object> implements MemberSet<E>, Recyclable {

		public abstract void add(E element);
	}


	public static class EmptySet<E extends Object> extends AbstractMemberSet<E> {

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#size()
		 */
		@Override
		public int size() {
			return 0;
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#elementAt(int)
		 */
		@Override
		public E elementAt(int index) {
			throw new IndexOutOfBoundsException();
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(E member) {
			return false;
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#recycle()
		 */
		@Override
		public void recycle() {
			// no-op
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#revive()
		 */
		@Override
		public boolean revive() {
			return true;
		}

		/**
		 * @see de.ims.icarus.model.standard.elements.MemberSets.AbstractMemberSet#add(java.lang.Object)
		 */
		@Override
		public void add(E element) {
			throw new UnsupportedOperationException();
		}

	}

	@HeapMember
	public static class SingletonSet<E extends Object> extends AbstractMemberSet<E> {

		@Reference(ReferenceType.DOWNLINK)
		private E item;

		public SingletonSet() {
			// no-op
		}

		public SingletonSet(E item) {
			reset(item);
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#size()
		 */
		@Override
		public int size() {
			if(item==null)
				throw new CorruptedStateException();

			return 1;
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#containerAt(int)
		 */
		@Override
		public E elementAt(int index) {
			if(item==null)
				throw new CorruptedStateException();
			if(index!=0)
				throw new IndexOutOfBoundsException();

			return item;
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#recycle()
		 */
		@Override
		public void recycle() {
			item = null;
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#revive()
		 */
		@Override
		public boolean revive() {
			return item!=null;
		}

		/**
		 *
		 * @see de.ims.icarus.model.api.members.MemberSet#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(E member) {
			if(item==null)
				throw new CorruptedStateException();
			if (member == null)
				throw new NullPointerException("Invalid member"); //$NON-NLS-1$
			return item==member;
		}

		public void reset(E member) {
			if (member == null)
				throw new NullPointerException("Invalid member"); //$NON-NLS-1$
			item = member;
		}

		/**
		 * @see de.ims.icarus.model.standard.elements.MemberSets.AbstractMemberSet#add(java.lang.Object)
		 */
		@Override
		public void add(E element) {
			if (element == null)
				throw new NullPointerException("Invalid element"); //$NON-NLS-1$
			if(item!=null)
				throw new CorruptedStateException("Element already set"); //$NON-NLS-1$
			item = element;
		}
	}

	@HeapMember
	public static class ArraySet<E extends Object> extends AbstractMemberSet<E> {

		@Reference(ReferenceType.DOWNLINK)
		private Object[] items;

		public ArraySet() {
			// no-op
		}

		@SafeVarargs
		public ArraySet(E...items) {
			reset(items);
		}

		public ArraySet(List<? extends E> items) {
			reset(items);
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#size()
		 */
		@Override
		public int size() {
			if(items==null)
				throw new CorruptedStateException();
			return items.length;
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#elementAt(int)
		 */
		@Override
		public E elementAt(int index) {
			if(items==null)
				throw new CorruptedStateException();
			@SuppressWarnings("unchecked")
			E item = (E) items[index];
			return item;
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#recycle()
		 */
		@Override
		public void recycle() {
			if(items!=null) {
				Arrays.fill(items, null);
			}
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#revive()
		 */
		@Override
		public boolean revive() {
			return items!=null && !CollectionUtils.contains(items, null);
		}

		public void reset(int size) {
			if(size<1)
				throw new IllegalArgumentException("Size must not be negative"); //$NON-NLS-1$

			if(items!=null && items.length==size) {
				return;
			}

			items = new Object[size];
		}

		public void set(int index, E member) {
			if(items==null)
				throw new CorruptedStateException();

			items[index] = member;
		}

		public void reset(Object[] elements) {
			if (elements == null)
				throw new NullPointerException("Invalid elements"); //$NON-NLS-1$

			items = elements;
		}

		public void reset(List<? extends E> elements) {
			if (elements == null)
				throw new NullPointerException("Invalid elements"); //$NON-NLS-1$

			items = new Object[elements.size()];
			elements.toArray(items);
		}

		/**
		 *
		 * @see de.ims.icarus.model.api.members.MemberSet#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(E member) {
			if(items==null)
				throw new CorruptedStateException();
			if (member == null)
				throw new NullPointerException("Invalid member"); //$NON-NLS-1$

			return CollectionUtils.contains(items, member);
		}

		/**
		 * @see de.ims.icarus.model.standard.elements.MemberSets.AbstractMemberSet#add(java.lang.Object)
		 */
		@Override
		public void add(E element) {
			if(items==null)
				throw new CorruptedStateException();
			if (element == null)
				throw new NullPointerException("Invalid element"); //$NON-NLS-1$

			for(int i=0; i<items.length; i++) {
				if(items[i]==null) {
					items[i] = element;
					return;
				}
			}

			throw new CorruptedStateException("Set already full"); //$NON-NLS-1$
		}
	}

	@HeapMember
	public static class CachedSet<E extends Object> extends AbstractMemberSet<E> {

		private final LookupList<E> items = new LookupList<>();

		public CachedSet() {
			// no-op
		}

		@SafeVarargs
		public CachedSet(E...items) {
			reset(items);
		}

		public CachedSet(List<? extends E> items) {
			reset(items);
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#size()
		 */
		@Override
		public int size() {
			return items.size();
		}

		/**
		 * @see de.ims.icarus.model.api.members.MemberSet#elementAt(int)
		 */
		@Override
		public E elementAt(int index) {
			return items.get(index);
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#recycle()
		 */
		@Override
		public void recycle() {
			items.clear();
		}

		/**
		 * @see de.ims.icarus.model.util.Recyclable#revive()
		 */
		@Override
		public boolean revive() {
			return !items.isEmpty();
		}

		public void reset(E[] members) {
			if (members == null)
				throw new NullPointerException("Invalid members"); //$NON-NLS-1$

			items.clear();

			items.addAll(members);
		}

		public void reset(List<? extends E> members) {
			if (members == null)
				throw new NullPointerException("Invalid members"); //$NON-NLS-1$

			items.clear();

			items.addAll(members);
		}

		/**
		 *
		 * @see de.ims.icarus.model.api.members.MemberSet#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(E member) {
			if (member == null)
				throw new NullPointerException("Invalid member"); //$NON-NLS-1$

			return items.contains(member);
		}

		/**
		 * @see de.ims.icarus.model.standard.elements.MemberSets.AbstractMemberSet#add(java.lang.Object)
		 */
		@Override
		public void add(E element) {
			if (element == null)
				throw new NullPointerException("Invalid element"); //$NON-NLS-1$thod stub

			items.add(element);
		}
	}
}
