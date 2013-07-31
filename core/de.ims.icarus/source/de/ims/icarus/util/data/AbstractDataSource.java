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
