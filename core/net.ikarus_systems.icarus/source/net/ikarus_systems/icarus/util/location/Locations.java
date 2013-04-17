/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.location;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class Locations {

	private Locations() {
		// no-op
	}
	

	public static Location getLocation(String path) throws MalformedURLException {
		if(path==null)
			throw new IllegalArgumentException("Invalid path"); //$NON-NLS-1$
		
		try {
			File file = new File(path);
			if(file.exists()) {
				return new DefaultFileLocation(file.getCanonicalFile());
			}
		} catch(IOException e) {
			// no-op
		}
		
		return new DefaultURLLocation(new URL(path));
	}
	
	public static String getPath(Location location) {
		if(location==null) {
			return null;
		}
		if(location.isLocal()) {
			return location.getFile().getAbsolutePath();
		} else {
			return location.getURL().toString();
		}
	}
	
	public static boolean equals(Location l1, Location l2) {
		if(l1==null && l2==null) {
			return true;
		}
		if(l1==null || l2==null) {
			return false;
		}
		
		if(l1.isLocal() && l2.isLocal()) {
			return l1.getFile().equals(l2.getFile());
		} else if(!l1.isLocal() && !l2.isLocal()) {
			return l1.getURL().equals(l2.getURL());
		} else {
			return false;
		}
	}
}
