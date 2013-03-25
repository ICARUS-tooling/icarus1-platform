/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.corpus;

import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.util.location.Location;

/**
 * Skeleton class for {@code Corpus} implementations. This class
 * provides all the common methods of basic corpora classes.
 * <p>
 * Note that it only provides many of the methods defined in the 
 * {@link Corpus} interface but does {@code not} implement the
 * interface itself! This is because there exist "sub-interfaces"
 * like {@link DerivedCorpus} and it should be up to the actually
 * implementing class to decide which particular interface to
 * implement.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class AbstractCorpus {
	
	protected Location location;
	protected EventSource eventSource = new EventSource(this);
	protected Map<String, Object> properties;
	
	protected String name = CorpusRegistry.getTempName((Corpus)this);
	
	
	@Override
	public String toString() {
		return getName();
	}
	
	public boolean isEditable() {
		return false;
	}
	
	public boolean hasGold() {
		return false;
	}

	public SentenceData getGold(int index, CorpusObserver observer) {
		return null;
	}

	public SentenceData getGold(int index) {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#setName(java.lang.String)
	 */
	public void setName(String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		this.name = name;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#saveState(net.ikarus_systems.icarus.language.corpus.CorpusDescriptor)
	 */
	public void saveState(CorpusDescriptor descriptor) {
		descriptor.setProperties(properties);
		descriptor.setName(getName());
		descriptor.setLocation(getLocation());
	}

	/**
	 * @see net.ikarus_systems.icarus.language.corpus.Corpus#loadState(net.ikarus_systems.icarus.language.corpus.CorpusDescriptor)
	 */
	public void loadState(CorpusDescriptor descriptor) {
		getProperties().putAll(descriptor.getProperties());
		name = descriptor.getName();
		location = descriptor.getLocation();
	}

	/**
	 * 
	 * @param item
	 * @throws UnsupportedSentenceDataException
	 * @throws UnsupportedOperationException
	 */
	public void add(SentenceData item) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @param item
	 * @param index
	 * @throws UnsupportedSentenceDataException
	 * @throws UnsupportedOperationException
	 */
	public void add(SentenceData item, int index) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @param item
	 * @throws UnsupportedSentenceDataException
	 * @throws UnsupportedOperationException
	 */
	public void remove(SentenceData item) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @param index
	 * @throws UnsupportedOperationException
	 */
	public void remove(int index) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the new {@code Location} to be used for this 
	 * {@code Corpus}. Subsequent calls to {@link #isLoaded()}
	 * will return {@code false} at least until the first call
	 * to {@link #load()} is performed 
	 * @param location the new {@code Location} to be used
	 */
	public void setLocation(Location location) {
		this.location = location;
		eventSource.fireEvent(new EventObject(CorpusEvents.LOCATION));
	}

	/**
	 * Returns the {@code Location} this {@code Corpus} is
	 * loading data from
	 * @return the {@code source} of this {@code Corpus}
	 */
	public Location getLocation() {
		return location;
	}

	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}
	
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new HashMap<>();
		}
		if(value==null) {
			properties.remove(key);
		} else {
			properties.put(key, value);
		}
	}
	
	public Map<String, Object> getProperties() {
		if(properties==null) {
			properties = new HashMap<>();
		}
		return properties;
	}
	
	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#addListener(java.lang.String, net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeListener(listener);
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.events.EventSource#removeListener(net.ikarus_systems.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeListener(listener, eventName);
	}
}
