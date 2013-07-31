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
package de.ims.icarus.ui.helper;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FocusOwnerTracker implements PropertyChangeListener{ 
    private static final String PERMANENT_FOCUS_OWNER_PROPERTY 
    		= "permanentFocusOwner";  //$NON-NLS-1$

    private Component comp;
    private boolean inside;
    
    private ChangeEvent changeEvent;
    
    /*
     * Only maintain a weak reference to the object that is
     * interesting in our tracking activity since we register ourselves
     * at a very long living property change source.
     */
    private WeakReference<ChangeListener> changeListener;

    public FocusOwnerTracker(Component comp, ChangeListener changeListener) { 
        this.comp = comp; 
        this.changeListener = changeListener==null ? null : new WeakReference<ChangeListener>(changeListener);
    } 

    public boolean isFocusInside() { 
        return checkFocus(false); 
    } 

    private boolean checkFocus(boolean find) { 
        if(!find) {
            return inside;
        }
        
        if(comp==null) {
        	return false;
        }

        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        if(c==null) {
        	return false;
        }
        return SwingUtilities.isDescendingFrom(c, comp);
    } 

    public void start() {
    	KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    	focusManager.removePropertyChangeListener(PERMANENT_FOCUS_OWNER_PROPERTY, this);
        focusManager.addPropertyChangeListener(PERMANENT_FOCUS_OWNER_PROPERTY, this); 
        inside = checkFocus(true);
    } 

    public void stop() { 
    	KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(PERMANENT_FOCUS_OWNER_PROPERTY, this); 
    } 

    @Override
    public void propertyChange(PropertyChangeEvent evt) { 
        boolean inside = checkFocus(true); 
        if(this.inside!=inside){ 
            this.inside = inside;
            notifyChangeListener();
        } 
    }
    
    private void notifyChangeListener() {
        if(changeListener!=null) {
        	if(changeEvent==null) {
        		changeEvent = new ChangeEvent(this);
        	}
        	ChangeListener listener = changeListener.get();
        	if(listener==null) {
        		stop();
        	} else {
        		listener.stateChanged(changeEvent);
        	}
        }
    }
    
    public void setComponent(Component comp) {
    	this.comp = comp;
    	inside = checkFocus(true);
    	notifyChangeListener();
    }
}