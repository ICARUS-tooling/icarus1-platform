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
package de.ims.icarus.util.mem;

import de.ims.icarus.util.collections.IdentityHashSet;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ObjectCache {

	private final IdentityHashSet<Object> cache = new IdentityHashSet<>();

	public synchronized boolean contains(Object object) {
		if (object == null)
			throw new NullPointerException("Invalid object"); //$NON-NLS-1$

		return cache.contains(object);
	}

	public synchronized boolean containsEquals(Object object) {
		if (object == null)
			throw new NullPointerException("Invalid object"); //$NON-NLS-1$

		return cache.containsEquals(object);
	}

	public synchronized boolean addIfAbsent(Object object) {
		return cache.add(object);
	}

	public synchronized boolean addIfAbsentEquals(Object object) {
		return cache.addEquals(object);
	}
}
