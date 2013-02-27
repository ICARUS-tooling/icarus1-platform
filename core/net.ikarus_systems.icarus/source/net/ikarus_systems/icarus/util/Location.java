/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util;

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
public interface Location {

	URL getURL();
	
	boolean isLocal();
	
	File getFile();
	
	OutputStream openOutputStream() throws IOException;
	
	InputStream openInputStream() throws IOException;
}
