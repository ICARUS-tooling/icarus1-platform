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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class Location {

	public abstract URL getURL();
	
	public abstract boolean isLocal();
	
	public abstract File getFile();
	
	public abstract OutputStream openOutputStream() throws IOException;
	
	public abstract InputStream openInputStream() throws IOException;
}
