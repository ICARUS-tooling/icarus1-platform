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


/**
 * A more complex version of the simple {@link Localizer}
 * with the ability to store localization related information
 * independent from the objects to be localized. This is
 * particularly useful when objects that require localization
 * offer no storage for localization related data (a common
 * example are some AWT members or custom objects)
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface RegisteringLocalizer extends Localizer {
	
	/**
	 * Stores localization data for a certain {@code Object}.
	 * The nature and meaning of the data being stored is highly
	 * implementation specific.
	 * @param item the item that requires localization
	 * @param data the data to be used when localizing the given {@code Object}
	 */
	void register(Object item, Object data);
}