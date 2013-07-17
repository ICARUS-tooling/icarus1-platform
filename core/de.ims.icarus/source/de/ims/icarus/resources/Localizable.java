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
 * An object that is able to localize itself without the
 * need of an external {@link Localizer}.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface Localizable {
	
	/**
	 * Reload localization data and refresh state.
	 */
	void localize();
}