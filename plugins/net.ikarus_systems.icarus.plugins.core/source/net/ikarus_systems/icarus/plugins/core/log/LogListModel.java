/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.util.Exceptions;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LogListModel extends Handler implements ListModel<LogRecord> {
	
	private List<LogRecord> records;
	private int maxSize;
	private List<ListDataListener> listDataListeners;
	private List<Integer> indexFilter;
	
	private List<ChangeListener> changeListeners;
	private int warningCount = 0;
	private int errorCount = 0;
	private ChangeEvent changeEvent = new ChangeEvent(this);

	public LogListModel() {
		this(1000);
	}

	public LogListModel(int maxSize) {
		setMaxSize(maxSize);
		setLevel(Level.ALL);
	}
	
	/**
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize the maxSize to set
	 */
	public void setMaxSize(int maxSize) {
		if(maxSize<100)
			throw new IllegalArgumentException("Maximum size value too small: "+maxSize); //$NON-NLS-1$
		
		this.maxSize = maxSize;
		
		if(records==null) {
			records = new ArrayList<>(Math.min(200, maxSize/2));
		} else if(records!=null && records.size()>maxSize) {
			records.subList(maxSize, records.size()-1).clear();
		}
	}
	
	public void clear() {
		synchronized (records) {
			if(indexFilter!=null)
				indexFilter.clear();

			int size = getSize();
			records.clear();
			warningCount = errorCount = 0;
			fireListDataEvent(new ListDataEvent(this, 
					ListDataEvent.INTERVAL_REMOVED, 0, size));
			fireStateChanged();
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		List<?> list = indexFilter==null ? records : indexFilter;
		
		return list==null ? 0 : list.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public LogRecord getElementAt(int index) {
		if(indexFilter!=null)
			index = indexFilter.get(index);
		
		return records.get(index);
	}

	/**
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(listDataListeners==null)
			listDataListeners = new ArrayList<>();
			
		listDataListeners.add(listener);
	}

	/**
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(listDataListeners!=null)
			listDataListeners.remove(listener);
	}
	
	private void fireListDataEvent(final ListDataEvent evt) {
		
		if(!SwingUtilities.isEventDispatchThread()) {
			UIUtil.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					fireListDataEvent(evt);
				}
			});
			
			return;
		}
		
		if(listDataListeners!=null) {
			for(ListDataListener listener : listDataListeners) {
				switch (evt.getType()) {
				case ListDataEvent.CONTENTS_CHANGED:
					listener.contentsChanged(evt);
					break;

				case ListDataEvent.INTERVAL_ADDED:
					listener.intervalAdded(evt);
					break;

				case ListDataEvent.INTERVAL_REMOVED:
					listener.intervalRemoved(evt);
					break;
				}
			}
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(changeListeners==null)
			changeListeners = new ArrayList<>();
			
		changeListeners.add(listener);
	}
	
	public void removeChangeListener(ChangeListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(changeListeners!=null)
			changeListeners.remove(listener);
	}
	
	private void fireStateChanged() {
		if(changeListeners!=null) {
			for(ChangeListener listener : changeListeners)
				listener.stateChanged(changeEvent);
		}
	}

	/**
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public void publish(final LogRecord record) {
		
		if(!SwingUtilities.isEventDispatchThread()) {
			UIUtil.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					publish(record);
				}
			});
			return;
		}
		
		boolean hasChanged = false;
		LogRecord removed = null;
		
		synchronized (records) {
			records.add(record);
			
			int fromIndex, toIndex;
			
			if(records.size()>maxSize) {
				removed = records.remove(0);
				fromIndex = 0;
				toIndex = records.size()-1;
				fireListDataEvent(new ListDataEvent(this, 
						ListDataEvent.CONTENTS_CHANGED, fromIndex, toIndex));
			}

			fromIndex = records.size()-1;
			toIndex = fromIndex;
			
			fireListDataEvent(new ListDataEvent(this, 
					ListDataEvent.INTERVAL_ADDED, fromIndex, toIndex));
		}
		
		int level = record.getLevel().intValue();
		if(level>=Level.WARNING.intValue() && level<Level.SEVERE.intValue()) {
			warningCount++;
			hasChanged = warningCount==1;
		} else if(level>=Level.SEVERE.intValue()) {
			errorCount++;
			hasChanged = errorCount==1;
		}
		
		if(removed!=null) {
			level = removed.getLevel().intValue();
			if(level>=Level.WARNING.intValue() && level<Level.SEVERE.intValue()) {
				warningCount--;
				hasChanged = warningCount==0;
			} else if(level>=Level.SEVERE.intValue()) {
				errorCount--;
				hasChanged = errorCount==0;
			}
		}
		
		if(hasChanged)
			fireStateChanged();
	}

	/**
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush() {
		// no-op
	}

	/**
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close() throws SecurityException {
		clear();
	}

	public int getWarningCount() {
		return warningCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

}
