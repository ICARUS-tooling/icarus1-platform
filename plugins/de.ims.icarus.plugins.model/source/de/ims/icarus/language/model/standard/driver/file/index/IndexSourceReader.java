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
package de.ims.icarus.language.model.standard.driver.file.index;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IndexSourceReader extends Closeable {

	boolean open() throws IOException;

	/**
	 * Returns the number of chunks in the underlying
	 * {@code ChunkIndex}.
	 *
	 * @return
	 */
	long getEntryCount();

	/**
	 * Returns the position at the file level a chunk specified
	 * via the {@code index} parameter is located at. Note that for
	 * chunk indices that only cover a single corpus file this method
	 * will always return {@code 0}
	 *
	 * @param index
	 * @return
	 */
	int getFileId(long index);
}
