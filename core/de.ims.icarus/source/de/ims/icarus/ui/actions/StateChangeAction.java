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
package de.ims.icarus.ui.actions;

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
