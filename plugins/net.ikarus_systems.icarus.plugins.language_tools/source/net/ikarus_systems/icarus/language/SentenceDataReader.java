/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

import java.io.Closeable;
import java.io.IOException;

import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedFormatException;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.UnsupportedLocationException;

/**
 * Deserialization handler for {@code SentenceData} objects.
 * It is used to sequentially access {@code SentenceData} from
 * arbitrary locations. Typically a {@code SentenceDataReader}
 * is not thread-safe since {@link #init(Location, Options)} could alter
 * the {@code location} of the reader from another thread while
 * the original {@code "owner-thread"} is still accessing data via
 * {@link #next()}. For this reason every entity that uses a reader
 * should obtain a private instance and not share it!
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface SentenceDataReader extends Closeable {
	
	public static final String INCLUDE_SYSTEM_OPTION = "includeSystem"; //$NON-NLS-1$	
	public static final String INCLUDE_GOLD_OPTION = "includeGold"; //$NON-NLS-1$
	public static final String INCLUDE_USER_OPTION = "includeUser"; //$NON-NLS-1$

	/**
	 * Sets the {@code Location} to load data from and initializes
	 * internal state so that calls to {@link #next()} will actually
	 * start to read {@code SentenceData} objects.
	 * 
	 * @param location the {@code Location} to load data from
	 * @param options a collection of additional info for the reader
	 * @throws IOException forwarding of encountered {@code IOException}s
	 * @throws UnsupportedLocationException if the provided {@code Location}
	 * is not supported or not valid
	 */
	void init(Location location, Options options) throws IOException, UnsupportedLocationException;
		
	/**
	 * Returns the next {@code SentenceData} object available or {@code null}
	 * if the end of the {@code "data stream"} is reached. {@code IOException}s
	 * should simply be forwarded to the calling method and in the case of
	 * data that is {@code unreadable} for this reader an {@code UnsupportedFormatException}
	 * should be thrown instead of returning {@code null}.
	 * @return the next {@code SentenceData} object available for this reader or
	 * {@code null} if the end of the {@code "data stream"} is reached
	 * @throws IOException simple forwarding of encountered {@code IOException}s
	 * @throws UnsupportedFormatException if the reader could not construct
	 * a new {@code SentenceData} object from the loaded data.
	 */
	SentenceData next() throws IOException, UnsupportedFormatException;
	
	/**
	 * Closes all underlying I/O-objects. Subsequent calls to {@link #next()}
	 * should throw {@code IOException}.
	 */
	void close() throws IOException;
	
	ContentType getContentType();
}
