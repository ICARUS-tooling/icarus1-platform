/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.actions;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class StateChangeAction extends DelegateAction implements ItemListener {

	private static final long serialVersionUID = 1994858029114851771L;

	private boolean selected = false;

	public StateChangeAction() {
		// no-op
	}

	public boolean isSelected() {
		return selected;
	}

	public synchronized void setSelected(boolean newValue) {
		boolean oldValue = this.selected;
		if (oldValue != newValue) {
			this.selected = newValue;
			firePropertyChange("selected", Boolean.valueOf(oldValue), //$NON-NLS-1$
					Boolean.valueOf(newValue));
		}
	}

	public synchronized void addItemListener(ItemListener listener) {
		getEventListenerList().add(ItemListener.class, listener);
	}

	public synchronized void removeItemListener(ItemListener listener) {
		getEventListenerList().remove(ItemListener.class, listener);
	}

	public synchronized ItemListener[] getItemListeners() {
		return getEventListenerList().getListeners(ItemListener.class);
	}

	public void itemStateChanged(ItemEvent evt) {
		boolean newValue = evt.getStateChange()==ItemEvent.SELECTED;
		boolean oldValue = this.selected;

		if (oldValue != newValue) {
			setSelected(newValue);

	        Object[] listeners = getEventListenerList().getListenerList();
	        
	        for (int i = listeners.length-2; i>=0; i-=2) {
	            if (listeners[i]==ItemListener.class) {
	                ((ItemListener)listeners[i+1]).itemStateChanged(evt);
	            }
	        }
		}
	}
}
