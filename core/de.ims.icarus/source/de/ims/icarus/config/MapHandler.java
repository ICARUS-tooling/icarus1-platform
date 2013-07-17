/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface MapHandler extends EntryHandler {
	
	void setKey(String key);
	
	String getKey();
}
