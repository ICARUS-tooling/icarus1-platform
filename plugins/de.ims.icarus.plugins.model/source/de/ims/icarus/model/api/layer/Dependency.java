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
package de.ims.icarus.model.api.layer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Dependency<E extends Object> {

	private final E target;
	private final DependencyType type;

	public Dependency(E target, DependencyType type) {
		if (target == null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$
		if (type == null)
			throw new NullPointerException("Invalid type"); //$NON-NLS-1$

		this.target = target;
		this.type = type;
	}

	public E getTarget() {
		return target;
	}

	public DependencyType getType() {
		return type;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return target.hashCode()*type.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Dependency) {
			Dependency<?> other = (Dependency<?>) obj;
			return target==other.target && type==other.type;
		}

		return false;
	}
}
