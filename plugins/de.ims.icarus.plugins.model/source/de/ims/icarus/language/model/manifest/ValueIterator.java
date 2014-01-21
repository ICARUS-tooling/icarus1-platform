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
package de.ims.icarus.language.model.manifest;

/**
 * Helper interface to allow an {@code OptionsManifest} to declare a
 * fixed set of possible values for a property. Note that the values
 * returned by this iterator's {@link #nextValue()} method can be either
 * the value objects themselves or instances of {@link ValueManifest}
 * that wrap an actual value and provide a textual description and/or name.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ValueIterator {

	/**
	 * Returns {@code true} iff there are more value definitions
	 * available for this iterator.
	 *
	 * @return {@code true} iff this iterator has at least one more
	 * value definitions available
	 */
	boolean hasMoreValues();

	/**
	 * Returns the next available value in this iterator.
	 * <p>
	 * Note that the returned object can be either
	 * the value object itself or and instance of {@link ValueManifest}
	 * that wraps an actual value and provide a textual description and/or name.
	 *
	 * @return
	 * @throws IllegalStateException if there are no more values
	 * available to be returned.
	 */
	Object nextValue();
}