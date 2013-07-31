/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.coref;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import de.ims.icarus.util.data.DataList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class CorefListMember<T extends Object> extends CorefMember implements DataList<T> {

	protected EventListenerList listenerList = new EventListenerList();
	
	protected List<T> items;
	
	protected CorefListMember() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return items==null ? 0 : items.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public T get(int index) {
		return items==null ? null : items.get(index);
	}
	
	public void add(T data) {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(items==null) {
			items = new ArrayList<>();
		}
		
		items.add(data);
		
		fireChangeEvent();
	}
	
	public void free() {
		items = null;
		properties = null;
		fireChangeEvent();
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
