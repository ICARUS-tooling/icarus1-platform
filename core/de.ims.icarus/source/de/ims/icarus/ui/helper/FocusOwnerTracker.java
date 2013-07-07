/*
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