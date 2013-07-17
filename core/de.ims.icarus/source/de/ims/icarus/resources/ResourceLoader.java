/*
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
