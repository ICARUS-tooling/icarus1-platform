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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.util;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultWrapper<O extends Object> implements Wrapper<O> {

	private final O element;

	private final boolean forwardEquals;
	private final boolean forwardHashCode;


	public DefaultWrapper(O element) {
		this(element, false, false);
	}

	public DefaultWrapper(O element, boolean forwardEquals, boolean forwardHashCode) {
		if(element==null)
			throw new NullPointerException("Invalid element"); //$NON-NLS-1$

		this.element = element;
		this.forwardEquals = forwardEquals;
		this.forwardHashCode = forwardHashCode;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if(forwardHashCode) {
			return element.hashCode();
		}
		return super.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(forwardEquals) {
			// Unwrap the target if it is a wrapper
			if(obj instanceof Wrapper) {
				obj = ((Wrapper<?>)obj).get();
			}
			return element.equals(obj);
		}
		return super.equals(obj);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return element.toString();
	}

	/**
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public O get() {
		return element;
	}
}
