/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.config;

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
