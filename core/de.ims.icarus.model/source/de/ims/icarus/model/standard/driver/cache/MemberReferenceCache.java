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
package de.ims.icarus.model.standard.driver.cache;

import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Primitive;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
@AccessControl(AccessPolicy.DENY)
public class MemberReferenceCache extends MemberCache {

	/**
	 * Inner class that acts as a data structure to create a new entry in the
	 * table.
	 */
	@HeapMember
	private static class ReferenceEntry extends Entry {
		@Primitive
		int refCount = 1;

		/**
		 * Create a new entry with the given values.
		 *
		 * @param key The key used to enter this in the table
		 * @param value The value for this key
		 * @param next A reference to the next entry in the table
		 */
		protected ReferenceEntry(long key, Item value, Entry next) {
			super(key, value, next);
		}
	}

	/**
	 * Retrieves the current reference counter for a given key
	 * @param key
	 * @return
	 */
	@AccessRestriction(AccessMode.ALL)
	public int getRefCount(long key) {
		ReferenceEntry entry = (ReferenceEntry)fetch(key);
		if(entry==null)
			throw new IllegalArgumentException("No member cached for key: "+key); //$NON-NLS-1$
		return entry.refCount;
	}

	/**
	 * Much like {@link #lookupMember(long)} this methods attempts to lookup a specific
	 * member. In addition, it increments the reference counter associated with the given key.
	 *
	 * @param key
	 * @return
	 */
	@AccessRestriction(AccessMode.MANAGE)
	public Item aquireMember(long key) {
		ReferenceEntry entry = (ReferenceEntry) fetch(key);
		if(entry==null) {
			return null;
		}

		entry.refCount++;

		return entry.value;
	}

	/**
	 * Called when a segment destroys its contents and frees a markable object.
	 * This method internally decreases the reference counter for the markable and
	 * once that counter reaches {@code 0}, removes the entry from the cache.
	 * In the case the entry is removed, returns the mapped {@link Item}, otherwise
	 * {@code null}.
	 * <p>
	 * Note that the cache does <b>not</b> automatically move released members into the
	 * object pool! Neither does it perform any special recycling operations on them to
	 * prepare them for getting pooled. It is up to surrounding client code to call
	 * the proper methods and finally push the member back to the cache again for pooling.
	 *
	 * @param key
	 * @return
	 */
	@AccessRestriction(AccessMode.MANAGE)
	public Item releaseMember(long key) {
		ReferenceEntry entry = (ReferenceEntry) fetch(key);
		if(entry==null)
			throw new IllegalArgumentException("No member registered for key: "+key); //$NON-NLS-1$

		if (--entry.refCount <= 0) {
			Item member = entry.value;
			remove(key);
			return member;
		}

		return null;
	}
}
