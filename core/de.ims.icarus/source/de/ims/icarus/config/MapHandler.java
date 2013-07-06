/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/config/MapHandler.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.config;

/**
 * 
 * @author Markus Gärtner
 * @version $Id: MapHandler.java 7 2013-02-27 13:18:56Z mcgaerty $
 *
 */
public interface MapHandler extends EntryHandler {
	
	void setKey(String key);
	
	String getKey();
}
