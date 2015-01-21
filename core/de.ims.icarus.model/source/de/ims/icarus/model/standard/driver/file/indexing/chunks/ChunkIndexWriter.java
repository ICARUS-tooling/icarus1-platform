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
package de.ims.icarus.model.standard.driver.file.indexing.chunks;

import java.io.Flushable;

import de.ims.icarus.model.io.SynchronizedAccessor;

/**
 * Defines the writer interface to get data into a {@link ChunkIndex}.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ChunkIndexWriter extends SynchronizedAccessor<ChunkIndex>, Flushable {

	/**
	 * Changes the file id for the given {@code index} to the new value
	 * and returns the old value if one was set.
	 *
	 * @param index
	 * @param fileId
	 * @return the value previously stored as file id for the given {@code index}
	 */
	int setFileId(long index, int fileId);

	long setBeginOffset(long index, long offset);

	long setEndOffset(long index, long offset);
}
