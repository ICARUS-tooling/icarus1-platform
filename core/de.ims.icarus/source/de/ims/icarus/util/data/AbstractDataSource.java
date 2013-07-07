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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractDataSource implements DataSource {
	
	private List<ChangeListener> listeners;
	
	private ChangeEvent changeEvent;

	protected AbstractDataSource() {
		// no-op
	}
	
	public void fireDataChanged() {
		if(listeners==null) {
			return;
		}
		
		for(ChangeListener listener : listeners) {
			if(changeEvent==null) {
				changeEvent = new ChangeEvent(this);
			}
			listener.stateChanged(changeEvent);
		}
	}

	/**
	 * @see de.ims.icarus.util.data.DataSource#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener l) {
		if(listeners==null) {
			listeners = new ArrayList<>();
		}
		
		listeners.add(l);
	}

	/**
	 * @see de.ims.icarus.util.data.DataSource#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener l) {
		if(listeners==null) {
			return;
		}
		
		listeners.remove(l);
	}

}
