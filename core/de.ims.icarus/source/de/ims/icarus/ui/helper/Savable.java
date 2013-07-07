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

import java.io.IOException;

import de.ims.icarus.util.location.Location;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Savable {

	void save(Location location) throws IOException;
}
