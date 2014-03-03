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
package de.ims.icarus.language.model.api.meta;

import java.util.Set;

import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationValueSet extends MetaData, Identity {

	/**
	 * Returns the type of values stored in this {@code ValueSet}
	 * @return
	 */
	ValueType getValueType();

	/**
	 * Returns all the possible values used for annotation.
	 *
	 * @return The non-empty collection of possible values for this
	 * value set
	 */
	Set<String> getValues();

	/**
	 * Provides a localized identification of a certain value.
	 * 
	 * @param key The <i>base-name</i> of the value to be localized.
	 * @return The (optionally localized) name of the provided value.
	 * @throws NullPointerException if the {@code value} is {@code null}
	 * @throws IllegalArgumentException if the given {@code value} is
	 * unknown to this value set
	 */
	String getName(String value);

	/**
	 * 
	 * Provides a localized description of a certain value.
	 * <p>
	 * This is an optional method.
	 * 
	 * @param key The <i>base-name</i> of the value to be localized.
	 * @return The (optionally localized) description of the provided value or {@code null}.
	 * @throws NullPointerException if the {@code value} is {@code null}
	 * @throws IllegalArgumentException if the given {@code key} is
	 * unknown to this value set
	 */
	String getDescription(String value);
}
