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
package de.ims.icarus.language.model.standard.corpus;

import de.ims.icarus.language.model.CorpusMember;
import de.ims.icarus.language.model.IdDomain;
import de.ims.icarus.util.collections.LongHashMap;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class IdPool implements IdDomain {

	private long min = 1;
	private long next = min;

	private LongHashMap<CorpusMember> lookup = new LongHashMap<>(1000);

	/**
	 * @see de.ims.icarus.language.model.IdDomain#getMinId()
	 */
	@Override
	public long getMinId() {
		return min;
	}

	/**
	 * @see de.ims.icarus.language.model.IdDomain#getMaxId()
	 */
	@Override
	public long getMaxId() {
		return -1;
	}

	/**
	 * @see de.ims.icarus.language.model.IdDomain#isRestricted()
	 */
	@Override
	public boolean isRestricted() {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.model.IdDomain#nextId()
	 */
	@Override
	public synchronized long nextId() {
		return next++;
	}

	/**
	 * @see de.ims.icarus.language.model.IdDomain#reserve(long)
	 */
	@Override
	public synchronized IdDomain reserve(long n) {
		if(n<1)
			throw new IllegalArgumentException("Invalid number of ids to reserve: "+n); //$NON-NLS-1$

		if(Long.MAX_VALUE-min-n < 0)
			throw new IllegalStateException("Unable to reserve number of ids: "+n); //$NON-NLS-1$

		IdDomain subDomain = new SubPool(next, n);

		next += n;

		return subDomain;
	}

	/**
	 * @see de.ims.icarus.language.model.IdDomain#lookup(long)
	 */
	@Override
	public CorpusMember lookup(long id) {
		synchronized (lookup) {
			CorpusMember member = lookup.get(id);

			if(member==null)
				throw new IllegalArgumentException("Unknown id: "+id); //$NON-NLS-1$

			return member;
		}
	}

	public void map(CorpusMember member) {
		if (member == null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		synchronized (lookup) {
			lookup.put(member.getId(), member);
		}
	}

	private class SubPool implements IdDomain {

		private final long min;
		private final long max;

		private long next;

		public SubPool(long min, long n) {
			this.min = min;
			this.max = n==-1 ? -1 : min+n;

			next = min;
		}

		/**
		 * @see de.ims.icarus.language.model.IdDomain#getMinId()
		 */
		@Override
		public long getMinId() {
			return min;
		}

		/**
		 * @see de.ims.icarus.language.model.IdDomain#getMaxId()
		 */
		@Override
		public long getMaxId() {
			return max;
		}

		/**
		 * @see de.ims.icarus.language.model.IdDomain#isRestricted()
		 */
		@Override
		public boolean isRestricted() {
			return max!=-1;
		}

		/**
		 * @see de.ims.icarus.language.model.IdDomain#nextId()
		 */
		@Override
		public long nextId() {
			if(max!=-1 && next>=max)
				throw new IllegalStateException("No more ids available. Max id reached: "+max); //$NON-NLS-1$

			return next++;
		}

		/**
		 * @see de.ims.icarus.language.model.IdDomain#reserve(long)
		 */
		@Override
		public IdDomain reserve(long n) {
			return IdPool.this.reserve(n);
		}

		/**
		 * @see de.ims.icarus.language.model.IdDomain#lookup(long)
		 */
		@Override
		public CorpusMember lookup(long id) {
			return IdPool.this.lookup(id);
		}

	}
}
