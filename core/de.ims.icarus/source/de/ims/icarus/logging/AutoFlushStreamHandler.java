/*
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
