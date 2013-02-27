/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.resources;


/**
 * A simple object to handle localization for other objects.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Localizer {
	
	/**
	 * Localizes the provided {@code Object}. The specific
	 * meaning of {@code localization} to be achieved is up
	 * to the individual implementation. 
	 * @param item the {@code Object} to localize
	 * @see Localizers
	 */
	void localize(Object item);
}