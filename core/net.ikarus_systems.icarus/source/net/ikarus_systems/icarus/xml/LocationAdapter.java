/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.util.DefaultURLLocation;
import net.ikarus_systems.icarus.util.Location;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationAdapter extends XmlAdapter<String, Location> {
	
	public Location unmarshal(String s) {
		try {
			return new DefaultURLLocation(new URL(s));
		} catch (MalformedURLException e) {
			return null;
		}
	}

	public String marshal(Location loc) {
		return loc.getURL().toExternalForm();
	}
}
