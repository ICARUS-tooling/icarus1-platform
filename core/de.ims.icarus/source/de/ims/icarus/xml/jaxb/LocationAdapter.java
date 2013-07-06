/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/xml/jaxb/LocationAdapter.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;


/**
 * @author Markus Gärtner
 * @version $Id: LocationAdapter.java 23 2013-04-17 12:39:04Z mcgaerty $
 *
 */
public class LocationAdapter extends XmlAdapter<String, Location> {
	
	public Location unmarshal(String s) {
		try {
			return Locations.getLocation(s);
		} catch (Exception e) {
			return null;
		}
	}

	public String marshal(Location loc) {
		return Locations.getPath(loc);
	}
}
