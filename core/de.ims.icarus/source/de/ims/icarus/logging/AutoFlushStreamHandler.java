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
package de.ims.icarus.logging;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * An extended version of the default {@code StreamHandler}
 * implementation that flushes the underlying {@code OutputStream}
 * every time there is a new {@code LogRecord} to be handled.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class AutoFlushStreamHandler extends StreamHandler {

	/**
	 * @param out
	 * @param formatter
	 */
	public AutoFlushStreamHandler(OutputStream out, Formatter formatter) {
		super(out, formatter);
	}

	/**
	 * Delegates the given {@code LogRecord} to the default implementation
	 * of {@code StreamHandler#flush()} in class {@code StreamHandler} and
	 * then calls {@link #flush()}.
	 * 
	 * @see java.util.logging.StreamHandler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		
		flush();
	}
}
