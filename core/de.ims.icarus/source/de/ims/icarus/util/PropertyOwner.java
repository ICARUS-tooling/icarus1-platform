/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util;

import java.util.Map;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface PropertyOwner {

	void setProperties(Map<String, Object> properties);
	
	Map<String, Object> getProperties();
}
