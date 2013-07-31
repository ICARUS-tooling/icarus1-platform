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
package de.ims.icarus.language;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
public abstract class AbstractMutableSentenceData implements
		MutableSentenceData {

	private static final long serialVersionUID = 5698632104051895600L;

	@XmlTransient
	protected transient List<SentenceDataListener> listeners;

	@XmlTransient
	protected boolean eventsEnabled = true;

	@Override
	public void addSentenceDataListener(SentenceDataListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		
		listeners.remove(listener);
		listeners.add(listener);
	}

	@Override
	public void removeSentenceDataListener(SentenceDataListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	/**
	 * @return the eventsEnabled
	 */
	public boolean isEventsEnabled() {
		return eventsEnabled;
	}

	/**
	 * @param eventsEnabled
	 *            the eventsEnabled to set
	 */
	public void setEventsEnabled(boolean eventsEnabled) {
		this.eventsEnabled = eventsEnabled;
	}

	@Override
	public abstract MutableSentenceData clone();
	
	protected boolean hasListeners() {
		return listeners!=null && !listeners.isEmpty();
	}

	public void fireDataChanged(SentenceDataEvent event) {
		if(listeners==null || !eventsEnabled) {
			return;
		}
		
		Object[] listeners = this.listeners.toArray();
		for(Object listener : listeners) {
			((SentenceDataListener)listener).dataChanged(event);
		}
	}
}
