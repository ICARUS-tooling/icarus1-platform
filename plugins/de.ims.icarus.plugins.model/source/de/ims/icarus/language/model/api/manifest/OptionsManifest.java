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
package de.ims.icarus.language.model.api.manifest;

import java.util.Set;

import de.ims.icarus.language.model.util.ValueType;

/**
 * Helper manifest (not describing a corpus member/entity of its own)
 * to specify possible properties the user can set on another manifest.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface OptionsManifest extends Derivable {

	/**
	 * Returns the names of all available options for the target
	 * manifest. This {@code Set} is guaranteed to be non-null and
	 * non-empty.
	 *
	 * @return The names of all available options as a {@code Set}
	 */
	Set<String> getOptionNames();

	/**
	 * Returns the default value for the property specified by the
	 * {@code name} argument
	 *
	 * @param name The {@code name} of the property for which the
	 * default value should be returned
	 * @return The default value for the specified property or {@code null}
	 * if the property has no default value assigned to it
	 * @throws NullPointerException if the {@code name} argument
	 * is {@code null}
	 */
	Object getDefaultValue(String name);

	/**
	 * Returns the type of the specified property. This method never
	 * returns {@code null}.
	 *
	 * @param name The {@code name} of the property for which the
	 * type should be returned
	 * @return The type for the specified property
	 * @throws NullPointerException if the {@code name} argument
	 * is {@code null}
	 */
	ValueType getValueType(String name);

	/**
	 * Returns a localized name string of the specified property, that
	 * is suitable for presentation in user interfaces.
	 *
	 * @param name The {@code name} of the property for which a
	 * localized name should be returned
	 * @return A localized name string for the specified property
	 * @throws NullPointerException if the {@code name} argument
	 * is {@code null}
	 */
	String getName(String name);

	/**
	 * Returns a localized description string of the specified property, that
	 * is suitable for presentation in user interfaces.
	 * <p>
	 * This is an optional method
	 *
	 * @param name The {@code name} of the property for which a
	 * localized description should be returned
	 * @return A localized description string for the specified property
	 * or {@code null} if there is no description available for it
	 * @throws NullPointerException if the {@code name} argument
	 * is {@code null}
	 */
	String getDescription(String name);

	/**
	 *
	 * @param name
	 * @return
	 */
	ValueSet getSupportedValues(String name);

	/**
	 *
	 * @param name
	 * @return
	 */
	ValueRange getSupportedRange(String name);

	/**
	 * Returns whether or not the option in question should be published
	 * to the user so he can modify it. Unpublished or <i>hidden</i> options
	 * are meant as a way of configuring implementations without allowing
	 * interference from the user.
	 *
	 * @param name
	 * @return
	 * @throws NullPointerException if the {@code name} argument
	 * is {@code null}
	 */
	boolean isPublished(String name);

	/**
	 * Returns whether an option is allowed to be assigned multiple values.
	 * This can be the case when the option in question presents the user a
	 * selective choice with several values.
	 *
	 * @param name
	 * @return
	 * @throws NullPointerException if the {@code name} argument
	 * is {@code null}
	 */
	boolean isMultiValue(String name);
}
