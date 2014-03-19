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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language.model.chunks;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Defines the writer interface to get data into a {@link ChunkIndex}.
 * It extends the {@link Closeable} and {@link Flushable} interfaces with
 * some extensions to their contract:
 * <br>
 * Calling either {@link Closeable#close()} or {@link Flushable#flush()} without
 * having first made a successful call to {@link #open()} inevitably yields an
 * exception as does any attempt to use the various setter methods for index data.
 * Once opened, the writer will be able to either directly pass data to the underlying
 * {@code ChunkIndex} or store changes until {@link #flush()} is called or it gets
 * closed, in which case it will automatically flush pending data changes. Note
 * that the calls for closing and opening need to be balanced (i.e. for each successful
 * call to {@code #open()} there can be at most one successful call to {@code #close()}).
 * Flushing on the other hand is not limited, as long as the writer stays open.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ChunkIndexWriter extends Closeable, Flushable {

	ChunkIndex getChunkIndex();

	boolean open() throws IOException;

	long clear() throws IOException;

	int setFileId(long index, int fileId);

	long setBeginOffset(long index, long offset);

	long setEndOffset(long index, long offset);

	long setBeginIndex(long index, long value);

	long setEndIndex(long index, long value);

	/**
	 * Returns whether or not this writer is able to handle the
	 * given {@code index}, i.e. if its implementation is capable
	 * of addressing or storing values in that magnitude. Note that
	 * any value that passes this check by returning {@code true} must
	 * not yield {@code IndexOutOfBoundsException}s when passed to any
	 * of the modification methods!
	 *
	 * @return
	 */
	boolean isIndexSupported(long index);
}
