/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractDataList<T extends Object> implements DataList<T> {
	
	protected EventListenerList listenerList = new EventListenerList();

	protected AbstractDataList() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}
	
	protected void fireChangeEvent() {
		Object[] listeners = listenerList.getListeners(ChangeListener.class);
		
		if(listeners==null || listeners.length==0) {
			return;
		}
		
		ChangeEvent evt = new ChangeEvent(this);
		
		for(Object listener : listeners) {
			((ChangeListener)listener).stateChanged(evt);
		}
	}
}
