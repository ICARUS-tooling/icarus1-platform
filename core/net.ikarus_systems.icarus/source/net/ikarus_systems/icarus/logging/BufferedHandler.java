/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class BufferedHandler extends Handler {

	public BufferedHandler() {
		// no-op
	}

	/**
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public void publish(LogRecord record) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub

	}

}
