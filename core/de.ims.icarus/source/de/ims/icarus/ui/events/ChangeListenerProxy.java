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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.events;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ChangeListenerProxy implements ChangeListener {

	
	private static Map<ChangeListener, ChangeListenerProxy> proxyCache;
	
	public static ChangeListenerProxy getProxy(ChangeListener listener) {
		if(listener==null)
			throw new NullPointerException("Invalid listener");
		
		if(proxyCache==null) {
			proxyCache = new WeakHashMap<>();
		}
		
		ChangeListenerProxy proxy = proxyCache.get(listener);
		
		if(proxy==null) {
			proxy = new ChangeListenerProxy(listener);
			proxyCache.put(listener, proxy);
		}
		
		return proxy;
	}
	
	private final Reference<ChangeListener> ref;

	public ChangeListenerProxy(ChangeListener owner) {
		if(owner==null)
			throw new NullPointerException("Invalid owner");
		
		this.ref = new WeakReference<ChangeListener>(owner);
	}
	
	private ChangeListener getListener(Object source) {
		ChangeListener listener = ref.get();
		if(listener==null && source instanceof ChangeSource) {
			((ChangeSource)source).removeChangeListener(this);
		}
		return listener;
	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		ChangeListener listener = getListener(e.getSource());
		if(listener!=null) {
			listener.stateChanged(e);
		}
	}
}
