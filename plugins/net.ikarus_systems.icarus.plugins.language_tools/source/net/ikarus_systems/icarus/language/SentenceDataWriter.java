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

import java.io.IOException;

import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.UnsupportedLocationException;
import net.ikarus_systems.icarus.util.location.Location;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface SentenceDataWriter {

	/**
	 * Sets the {@code Location} to save data to and initializes
	 * internal state so that calls to {@link #write(SentenceData)} 
	 * will actually start to save {@code SentenceData} objects.
	 * The {@code options} parameter is meant to be a hint for
	 * certain implementations on how they should format output
	 * data or whether to write additional meta-data.
	 * 
	 * @param location the {@code Location} to save data to
	 * @param options a collection of additional info for the writer
	 * @throws IOException forwarding of encountered {@code IOException}s
	 * @throws UnsupportedLocationException if the provided {@code Location}
	 * is not supported or not valid
	 */
	void init(Location location, Options options) throws IOException, UnsupportedLocationException;
			
	/**
	 * 
	 * @param data the next {@code SentenceData} object to be written
	 * @throws IOException simple forwarding of encountered {@code IOException}s
	 * @throws UnsupportedSentenceDataException if the given {@code SentenceData}
	 * is of a type this writer is unable to handle.
	 */
	void write(SentenceData data) throws IOException, UnsupportedSentenceDataException;
	
	/**
	 * Closes all underlying I/O-objects. Subsequent calls to 
	 * {@link SentenceDataWriter#write(SentenceData)} should throw 
	 * {@code IOException}. Note that this method might be called
	 * before a 'logical set' of data has been written or even while
	 * a write operation is currently in progress. It is up to the
	 * implementation at hand to decide whether data written so far
	 * should be discarded, meaning the {@code location} being deleted,
	 * or if the output is allowed to stay in a state that could be
	 * described as 'unfinished'. For example a writer that uses 
	 * {@code xml} to format {@code SentenceData} objects will typically
	 * discard all data before risking invalid {@code xml}-code.
	 */
	void close();
}
