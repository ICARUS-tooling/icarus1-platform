/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/resources/Localizer.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.resources;


/**
 * A simple object to handle localization for other objects.
 * 
 * @author Markus Gärtner 
 * @version $Id: Localizer.java 7 2013-02-27 13:18:56Z mcgaerty $
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