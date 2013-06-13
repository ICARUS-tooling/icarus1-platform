/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.io;

import java.io.Closeable;
import java.io.IOException;

import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

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
