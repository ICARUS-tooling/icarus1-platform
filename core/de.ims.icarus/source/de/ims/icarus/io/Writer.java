/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.io;

import java.io.Closeable;
import java.io.IOException;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Writer<T extends Object> extends Closeable {

	void init(Location location, Options options) throws IOException, UnsupportedLocationException;

	void write(T data) throws IOException, UnsupportedFormatException;
	
	void close() throws IOException;
}
