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
import java.io.Flushable;
import java.io.IOException;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface IndexSourceWriter extends Closeable, Flushable {

	/**
	 * Attempts to open a data channel to the underlying chunk index and
	 * returns true if not already open.
	 *
	 * @return
	 * @throws IOException
	 */
	boolean open() throws IOException;

	/**
	 * Erases all data in the underlying index source.
	 * @return
	 * @throws IOException
	 */
	long clear() throws IOException;

	int setFileId(long index, int fileId);

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
