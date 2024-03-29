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
package de.ims.icarus.util.data;

import java.util.Map;

import de.ims.icarus.util.Filter;
import de.ims.icarus.util.id.Identity;


/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ContentType extends Identity, Filter {

	/**
	 * Returns the root class or interface that objects associated
	 * with this {@code ContentType} must extend or implement.
	 * This method must not return {@code null} but is technically
	 * allowed to return the {@link Object} class.
	 */
	Class<?> getContentClass();

	String getContentClassName();

	/**
	 * Returns a read-only collection of properties in the form of
	 * a key-value mapping. If this {@code ContentType} does not
	 * declare any properties it may either return an empty map or
	 * {@code null}.
	 */
	Map<String, Object> getProperties();

	/**
	 * Checks the given argument (typically a {@code Class} instance)
	 * for compatibility with this content-type.
	 * <p>
	 * Note that although all members of the content type framework
	 * will call this method with a valid {@code Class} argument, it
	 * not explicitly required to do so! Custom implementations that
	 * are designed to be used outside the framework might want to handle
	 * arbitrary argument objects.
	 *
	 * @see de.ims.icarus.util.Filter#accepts(java.lang.Object)
	 */
	@Override
	boolean accepts(Object obj);

	/**
	 * Signals whether content is only allowed to be of the class
	 * returned by {@link #getContentClass()} and not of some subclass
	 * of it.
	 * <p>
	 * The property type is {@code boolean} and this property is only
	 * effective when the return value of {@link #getContentClass()}
	 * is not a class describing an interface, array or enum.
	 */
	public static final String STRICT_INHERITANCE = "strictInheritance"; //$NON-NLS-1$
}
