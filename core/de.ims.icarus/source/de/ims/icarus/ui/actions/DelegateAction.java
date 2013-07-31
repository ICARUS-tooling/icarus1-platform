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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.event.EventListenerList;

/**
 * @author Markus Gärtner
 * @version $Id$
 * 
 */
public class DelegateAction extends AbstractAction {

	private static final long serialVersionUID = -1584115051535868534L;

	private transient EventListenerList listenerList;

	public DelegateAction() {
		// no-op
	}
	
	protected EventListenerList getEventListenerList() {
		if(listenerList==null)
			listenerList = new EventListenerList();
		
		return listenerList;
	}

	public synchronized void addActionListener(ActionListener listener) {
		getEventListenerList().add(ActionListener.class, listener);
	}

	public synchronized void removeActionListener(ActionListener listener) {
		getEventListenerList().remove(ActionListener.class, listener);
	}

	public synchronized ActionListener[] getActionListeners() {
		return getEventListenerList().getListeners(ActionListener.class);
	}

	public void actionPerformed(ActionEvent evt) {
        Object[] listeners = getEventListenerList().getListenerList();
        
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                ((ActionListener)listeners[i+1]).actionPerformed(evt);
            }
        }
	}
}
