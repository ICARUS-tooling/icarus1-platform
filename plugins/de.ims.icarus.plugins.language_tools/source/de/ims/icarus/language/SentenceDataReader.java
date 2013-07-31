/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language;

import java.io.IOException;

import de.ims.icarus.io.Reader;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.UnsupportedLocationException;


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
public interface SentenceDataReader extends Reader<SentenceData> {
	
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
