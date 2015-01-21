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

import java.util.ArrayList;
import java.util.Collection;

import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.util.mem.Primitive;
import de.ims.icarus.util.mem.Reference;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MemberPool {

	@Reference
	private ArrayList<Item> pool;

	@Primitive
	private final int poolSize;

	public MemberPool() {
		this(10000);
	}

	public MemberPool(int poolSize) {
		if (poolSize <= 0)
			throw new IllegalArgumentException("Illegal pool-size (negative or zero): " //$NON-NLS-1$
					+ poolSize);
		this.poolSize = poolSize;
	}


	/**
	 * Adds the given member to the internal object pool.
	 * Note that if the pool is full this method does nothing
	 * and simply discards the given {@code member}.
	 * @param member
	 */
	public boolean recycle(Item member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		if(pool==null) {
			synchronized (this) {
				if(pool==null) {
					pool = new ArrayList<>(poolSize);
				}
			}
		}

		if(pool.size()<poolSize) {
			pool.add(member);
			return true;
		}

		return false;
	}

	/**
	 * Returns the last pooled member or {@code null} if there currently is
	 * no object pooled.
	 * @return
	 */
	public Item revive() {
		if(pool==null || pool.isEmpty()) {
			return null;
		}

		return pool.remove(pool.size()-1);
	}

	public void recycleAll(Collection<? extends Item> members) {
		if (members == null)
			throw new NullPointerException("Invalid members"); //$NON-NLS-1$

		if(pool==null) {
			pool = new ArrayList<>(poolSize);
		}

		for(Item member : members) {
			if(pool.size()>=poolSize) {
				break;
			}

			pool.add(member);
		}
	}
}
