/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

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
