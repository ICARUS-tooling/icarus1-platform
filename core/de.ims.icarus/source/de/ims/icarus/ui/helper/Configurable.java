/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.helper;



/**
 * Serves as a kind of "flag" to mark objects that can be "configured"
 * by the user in some way. The {@link #openConfig()} method serves as
 * delegate to access the real configuration. This does not have to be
 * the default {@code ConfigDialog} implementation but can be something
 * as simple as a little {@code Dialog} with some input components like
 * text-fields or check-boxes. It is recommended that any class that
 * wishes to be presented to the user as part of some UI should implement
 * this interface in case it features configuration possibilities.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Configurable {

	/**
	 * Opens and presents the user the configuration interface associated
	 * with this object. The nature of this "interface" is implementation
	 * specific and is not restricted to the default {@code ConfigDialog}
	 * used to access a {@code ConfigStorage}.
	 */
	void openConfig();
}
