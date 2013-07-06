/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/resources/Localizable.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.resources;

/**
 * An object that is able to localize itself without the
 * need of an external {@link Localizer}.
 * 
 * @author Markus Gärtner 
 * @version $Id: Localizable.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public interface Localizable {
	
	/**
	 * Reload localization data and refresh state.
	 */
	void localize();
}