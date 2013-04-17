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

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class BufferedHandler extends Handler {
	
	private LogRecord[] buffer;
	private int startOffset = 0;
	private int size = 0;
	
	private List<ChangeListener> changeListeners;
	private ChangeEvent changeEvent;
	
	private static final int MAX_RECORDS = 3200;
	private static final int START_RECORDS = 100;
	
	public BufferedHandler() {
		// no-op
	}

	/**
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public synchronized void publish(LogRecord record) {
		if(buffer==null) {
			buffer = new LogRecord[START_RECORDS];
		}
		
		// Get offset based on relative start index
		int offset = startOffset + size;
		
		// Expand buffer if required
		if(offset >= buffer.length && buffer.length < MAX_RECORDS) {
			int newSize = Math.min(buffer.length*2, MAX_RECORDS);
			LogRecord[] newBuffer = new LogRecord[newSize];
			System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
		}
		
		// Get real index
		if(offset >= buffer.length) {
			offset -= buffer.length;
		}
		
		buffer[offset] = record;
		
		// Shift start offset if required
		if(offset >= startOffset) {
			startOffset++;
			if(startOffset >= buffer.length) {
				startOffset -= buffer.length;
			}
		} else {
			// Size only increases if we do not have to shift the start
			// index (i.e. remove the oldest record)
			size++;
		}
		
		// TODO notify some listeners?
	}

	/**
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush() {
		// no-op
	}
	
	public synchronized void clear() {
		if(buffer==null) {
			return;
		}
		
		startOffset = 0;
		size = 0;
		Arrays.fill(buffer, null);
	}

	/**
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close() throws SecurityException {
		clear();
	}

	public synchronized int getRecordCount() {
		return size;
	}
	
	public synchronized LogRecord getRecord(int index) {
		if(buffer==null) {
			return null;
		}
		
		if(index>=size)
			throw new IndexOutOfBoundsException("Invalid index: "+index); //$NON-NLS-1$
		
		int offset = startOffset + index;
		if(offset >= buffer.length) {
			offset -= buffer.length;
		}
		
		return buffer[offset];
	}
}
