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