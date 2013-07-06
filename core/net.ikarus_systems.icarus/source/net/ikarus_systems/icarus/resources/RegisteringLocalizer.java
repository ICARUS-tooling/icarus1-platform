/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/resources/RegisteringLocalizer.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.resources;


/**
 * A more complex version of the simple {@link Localizer}
 * with the ability to store localization related information
 * independent from the objects to be localized. This is
 * particularly useful when objects that require localization
 * offer no storage for localization related data (a common
 * example are some AWT members or custom objects)
 * 
 * @author Markus Gärtner 
 * @version $Id: RegisteringLocalizer.java 7 2013-02-27 13:18:56Z mcgaerty $
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