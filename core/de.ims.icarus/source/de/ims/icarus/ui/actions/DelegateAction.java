/*
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
