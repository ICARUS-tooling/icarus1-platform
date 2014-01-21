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
 * A helper class that wraps a value and provides additional textual information
 * like a description and an optional name. The purpose of those strings is so
 * that user interfaces can provide the user with information about the available
 * values for an option.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ValueManifest {

	/**
	 * Returns the value this manifest wraps and describes.
	 *
	 * @return
	 */
	Object getValue();

	/**
	 * Returns the (optional) name for this value, which is not required to be
	 * localized.
	 * <p>
	 * This is an optional method.
	 *
	 * @return The name of this value or {@code null} if the value is unnamed
	 */
	String getName();

	/**
	 * Returns the (preferably localized) textual description of this value.
	 *
	 * @return A textual description of this value
	 */
	String getDescription();
}