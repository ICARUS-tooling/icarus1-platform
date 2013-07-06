/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Installable {

	/**
	 * Installs callbacks, listeners and other stuff related to the
	 * target argument.
	 */
	void install(Object target);
	
	/**
	 * Releases all resources associated with the target and
	 * unregisters listeners and callbacks.
	 */
	void uninstall(Object target);
}
