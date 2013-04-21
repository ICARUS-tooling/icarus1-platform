/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank;

import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.language.SentenceData;
import net.ikarus_systems.icarus.language.UnsupportedSentenceDataException;
import net.ikarus_systems.icarus.ui.events.EventListener;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.events.EventSource;
import net.ikarus_systems.icarus.util.location.Location;

/**
 * Skeleton class for {@code Treebank} implementations. This class
 * provides all the common methods of basic treebank classes.
 * <p>
 * Note that it only provides many of the methods defined in the 
 * {@link Treebank} interface but does {@code not} implement the
 * interface itself! This is because there exist "sub-interfaces"
 * like {@link DerivedTreebank} and it should be up to the actually
 * implementing class to decide which particular interface to
 * implement.
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public abstract class AbstractTreebank {
	
	protected Location location;
	protected EventSource eventSource = new EventSource(this);
	protected Map<String, Object> properties;
	
	protected String name = TreebankRegistry.getTempName((Treebank)this);
	
	
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

	public SentenceData getGold(int index, TreebankObserver observer) {
		return null;
	}

	public SentenceData getGold(int index) {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#setName(java.lang.String)
	 */
	public void setName(String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		this.name = name;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#saveState(net.ikarus_systems.icarus.language.treebank.TreebankDescriptor)
	 */
	public void saveState(TreebankDescriptor descriptor) {
		descriptor.setProperties(properties);
		descriptor.setName(getName());
		descriptor.setLocation(getLocation());
	}

	/**
	 * @see net.ikarus_systems.icarus.language.treebank.Treebank#loadState(net.ikarus_systems.icarus.language.treebank.TreebankDescriptor)
	 */
	public void loadState(TreebankDescriptor descriptor) {
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
	 * {@code Treebank}. Subsequent calls to {@link #isLoaded()}
	 * will return {@code false} at least until the first call
	 * to {@link #load()} is performed 
	 * @param location the new {@code Location} to be used
	 */
	public void setLocation(Location location) {
		this.location = location;
		eventSource.fireEvent(new EventObject(TreebankEvents.LOCATION));
	}

	/**
	 * Returns the {@code Location} this {@code Treebank} is
	 * loading data from
	 * @return the {@code source} of this {@code Treebank}
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
