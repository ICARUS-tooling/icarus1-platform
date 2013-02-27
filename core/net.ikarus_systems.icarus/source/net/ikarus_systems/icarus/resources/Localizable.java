/*
 * $Revision: 262 $
 * $Date: 2012-12-22 22:30:56 +0100 (Sa, 22 Dez 2012) $
 * $URL: https://subversion.assembla.com/svn/ims-studienarbeit/trunk/Icarus/core/net.ikarus.systems.icarus.platform.launcher/source/net/ikarus/systems/icarus/platform/launcher/Launcher.java $
 *
 * $LastChangedDate: 2012-12-22 22:30:56 +0100 (Sa, 22 Dez 2012) $ 
 * $LastChangedRevision: 262 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.resources;

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