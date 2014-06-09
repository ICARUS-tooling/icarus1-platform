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
package de.ims.icarus.language.model.io;

import de.ims.icarus.language.model.api.CorpusException;

/**
 * Models a reader or writer linked to a specific source that can be used by multiple
 * threads. Its individual accessor methods do not have to contain synchronizing
 * or locking code, but rather the locking of its underlying resources is done
 * via the {@link #begin()} method and the current lock is releases by
 * calling {@link #end()}.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface SynchronizedAccessor<E extends Object> extends AutoCloseable {

	E getSource();

	void begin();

	void end();

	@Override
	void close() throws CorpusException;
}