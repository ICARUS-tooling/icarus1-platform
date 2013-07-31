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
package de.ims.icarus.resources;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A {@code ResourceLoader} is responsible for fetching
 * arbitrary resources (normally during the process of localization)
 * in a transparent way. This approach separates the management
 * of localization data from the actual loading process.
 * The {@link ResourceManager} calls {@code #loadResource(String, Locale)}
 * on certain instances of {@code ResourceLoader} whenever there is
 * the need to load new data for a given combination of {@link Locale}
 * and {@code name} where the exact semantic of {@code name} is 
 * implementation specific (it can denote a resource path in the
 * way of fully qualified resource naming or the remote location
 * of a resource bundle available over the Internet).
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface ResourceLoader {

	/**
	 * Attempts to load a new {@code ResourceBundle} for the given 
	 * combination of {@code Locale} and {@code name}. Implementations
	 * should throw an {@code MissingResourceException} when encountering
	 * errors or when there is no matching resource data in the
	 * domain of this {@code ResourceLoader}. 
	 * 
	 * @param name abstract identifier for the resource in question
	 * @param locale the {@code Locale} associated with the resource
	 * in question
	 * @return the new {@code ResourceBundle} for the given combination of 
	 * {@code Locale} and {@code name}
	 * @throws MissingResourceException if the desired resource could
	 * not be found
	 */
	ResourceBundle loadResource(String name, Locale locale);
}
