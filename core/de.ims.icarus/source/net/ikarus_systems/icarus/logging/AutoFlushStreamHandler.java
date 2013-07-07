/*
 * $Revision: 7 $
 * $Date: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/logging/AutoFlushStreamHandler.java $
 *
 * $LastChangedDate: 2013-02-27 14:18:56 +0100 (Mi, 27 Feb 2013) $ 
 * $LastChangedRevision: 7 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.logging;

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
 * @version $Id: AutoFlushStreamHandler.java 7 2013-02-27 13:18:56Z mcgaerty $
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
