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
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Reader<T extends Object> extends Closeable {

	void init(Location location, Options options) throws IOException, UnsupportedLocationException;

	T next() throws IOException, UnsupportedFormatException;
	
	/**
	 * Closes all underlying I/O-objects. Subsequent calls to {@link #next()}
	 * should throw {@code IOException}.
	 */
	void close() throws IOException;
	
	ContentType getContentType();
}
