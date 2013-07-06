/*
 * $Revision: 17 $
 * $Date: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/util/location/Location.java $
 *
 * $LastChangedDate: 2013-03-25 01:44:03 +0100 (Mo, 25 Mrz 2013) $ 
 * $LastChangedRevision: 17 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.util.location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Markus Gärtner 
 * @version $Id: Location.java 17 2013-03-25 00:44:03Z mcgaerty $
 *
 */
public abstract class Location {

	public abstract URL getURL();
	
	public abstract boolean isLocal();
	
	public abstract File getFile();
	
	public abstract OutputStream openOutputStream() throws IOException;
	
	public abstract InputStream openInputStream() throws IOException;
}
