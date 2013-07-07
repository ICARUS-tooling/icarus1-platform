/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.helper;

import java.io.IOException;

import net.ikarus_systems.icarus.util.location.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Savable {

	void save(Location location) throws IOException;
}
