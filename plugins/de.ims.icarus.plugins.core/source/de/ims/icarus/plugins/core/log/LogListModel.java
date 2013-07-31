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
package de.ims.icarus.plugins.core.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.Events;
import de.ims.icarus.util.Exceptions;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LogListModel implements ListModel<LogRecord>, EventListener {
	
	private int offset = 0;
	
	private List<ListDataListener> listDataListeners;
	
	private List<ChangeListener> changeListeners;
	private int warningCount = 0;
	private int errorCount = 0;
	private ChangeEvent changeEvent = new ChangeEvent(this);

	public LogListModel() {
		LoggerFactory.getRootHandler().addListener(Events.ADDED, this);
		LoggerFactory.getRootHandler().addListener(Events.REMOVED, this);
		LoggerFactory.getRootHandler().addListener(Events.CLEANED, this);
		
		initCounts();
	}
	
	private void initCounts() {
		for(int i=0; i<getSize(); i++) {
			LogRecord record = getElementAt(i);
			int level = record.getLevel().intValue();
			if(level>=Level.SEVERE.intValue()) {
				errorCount++;
			} else if(level>=Level.WARNING.intValue()) {
				warningCount++;
			}
		}
	}
	
	public void clear() {
		int toIndex = Math.max(0, getSize()-1);
		offset = LoggerFactory.getRootHandler().getRecordCount();
		
		warningCount = 0;
		errorCount = 0;

		fireListDataEvent(new ListDataEvent(this, 
				ListDataEvent.INTERVAL_REMOVED, 0, toIndex));
		fireStateChanged();
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return Math.max(0, LoggerFactory.getRootHandler().getRecordCount()-offset);
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public LogRecord getElementAt(int index) {
		index += offset;
		
		return index>=LoggerFactory.getRootHandler().getRecordCount() ?
				null : LoggerFactory.getRootHandler().getRecord(index);
	}

	/**
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(listDataListeners==null) {
			listDataListeners = new ArrayList<>();
		}
			
		listDataListeners.add(listener);
	}

	/**
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener listener) {
		Exceptions.testNullArgument(listener, "listener"); //$NON-NLS-1$
		
		if(listDataListeners!=null) {
			listDataListeners.remove(listener);
		}
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
		
		if(!SwingUtilities.isEventDispatchThread()) {
			UIUtil.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					fireStateChanged();
				}
			});
			
			return;
		}
		
		if(changeListeners!=null) {
			for(ChangeListener listener : changeListeners) {
				listener.stateChanged(changeEvent);
			}
		}
	}

	public void close() {
		LoggerFactory.getRootHandler().removeListener(this);
	}

	public int getWarningCount() {
		return warningCount;
	}

	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * @see de.ims.icarus.ui.events.EventListener#invoke(java.lang.Object, de.ims.icarus.ui.events.EventObject)
	 */
	@Override
	public void invoke(Object sender, EventObject event) {
		if(Events.ADDED.equals(event.getName())) {
			LogRecord record = (LogRecord) event.getProperty("record"); //$NON-NLS-1$
			int index = (int) event.getProperty("index"); //$NON-NLS-1$
            int level = record.getLevel().intValue();
	        boolean hasChanged = false;
	        if(level>=Level.SEVERE.intValue()) {
	            errorCount++;
	            hasChanged = errorCount==1;
	        } else if(level>=Level.WARNING.intValue()) {
	            warningCount++;
	            hasChanged = warningCount==1;
	        }

            index -= offset;
            fireListDataEvent(new ListDataEvent(this,
                    ListDataEvent.INTERVAL_ADDED, index, index));
            if(hasChanged) {
            	fireStateChanged();
            }
			
		} else if(Events.REMOVED.equals(event.getName())) {
			LogRecord removed = (LogRecord) event.getProperty("record"); //$NON-NLS-1$
			int index = (int) event.getProperty("index"); //$NON-NLS-1$
            int level = removed.getLevel().intValue();
            boolean hasChanged = false;
            if(level>=Level.SEVERE.intValue()) {
                errorCount--;
                hasChanged = errorCount==0;
            } else if(level>=Level.WARNING.intValue()) {
                warningCount--;
                hasChanged = warningCount==0;
            }

            index -= offset;
            fireListDataEvent(new ListDataEvent(this,
                    ListDataEvent.CONTENTS_CHANGED, index, index));
            if(hasChanged) {
            	fireStateChanged();
            }
            
		} else if(Events.CLEANED.equals(event.getName())) {
			clear();
		}
	}

}
