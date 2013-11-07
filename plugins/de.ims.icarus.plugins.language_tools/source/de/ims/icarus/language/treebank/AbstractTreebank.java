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
package de.ims.icarus.language.treebank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.ui.events.EventListener;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.EventSource;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.location.Location;


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
	
	protected List<ChangeListener> changeListeners;
	
	
	@Override
	public String toString() {
		return getName();
	}
	
	public boolean isEditable() {
		return false;
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#setName(java.lang.String)
	 */
	public void setName(String name) {
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$
		this.name = name;
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @see Identity#getId()
	 */
	public String getId() {
		return TreebankRegistry.getInstance().getDescriptor((Treebank)this).getId();
	}
	
	public Object getOwner() {
		return this;
	}
	
	public String getDescription() {
		return null;
	}
	
	public Icon getIcon() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#saveState(de.ims.icarus.language.treebank.TreebankDescriptor)
	 */
	public void saveState(TreebankDescriptor descriptor) {
		descriptor.setProperties(properties);
		descriptor.setName(getName());
		descriptor.setLocation(getLocation());
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#loadState(de.ims.icarus.language.treebank.TreebankDescriptor)
	 */
	public void loadState(TreebankDescriptor descriptor) {
		getProperties().putAll(descriptor.getProperties());
		name = descriptor.getName();
		location = descriptor.getLocation();
	}

	/**
	 * Sets the new {@code Location} to be used for this 
	 * {@code Treebank}. Subsequent calls to {@link #isLoaded()}
	 * will return {@code false} at least until the first call
	 * to {@link #load()} is performed 
	 * @param location the new {@code Location} to be used
	 */
	public void setLocation(Location location) {
		if(this.location!=null && this.location.equals(location)) {
			return;
		}
		
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
	
	public SentenceData get(int index) {
		return ((Treebank)this).get(index, DataType.SYSTEM);
	}
	
	/**
	 * @see de.ims.icarus.ui.events.EventSource#addListener(java.lang.String, de.ims.icarus.ui.events.EventListener)
	 */
	public void addListener(String eventName, EventListener listener) {
		eventSource.addListener(eventName, listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener)
	 */
	public void removeListener(EventListener listener) {
		eventSource.removeEventListener(listener);
	}

	/**
	 * @see de.ims.icarus.ui.events.EventSource#removeEventListener(de.ims.icarus.ui.events.EventListener, java.lang.String)
	 */
	public void removeListener(EventListener listener, String eventName) {
		eventSource.removeEventListener(listener, eventName);
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#set(de.ims.icarus.language.SentenceData, int, de.ims.icarus.language.DataType)
	 */
	public void set(SentenceData item, int index, DataType type) {
		if(!isEditable())
			throw new UnsupportedOperationException();
	}

	/**
	 * @see de.ims.icarus.language.treebank.Treebank#remove(int, de.ims.icarus.language.DataType)
	 */
	public void remove(int index, DataType type) {
		if(!isEditable())
			throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @see SentenceDataList#addChangeListener(ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		if(changeListeners==null) {
			changeListeners = new ArrayList<>();
		}
		
		changeListeners.add(listener);
	}
	
	/**
	 * @see SentenceDataList#removeChangeListener(ChangeListener)
	 */
	public void removeChangeListener(ChangeListener listener) {
		if(changeListeners==null) {
			return;
		}
		
		changeListeners.remove(listener);
	}
	
	protected void fireChangeEvent() {
		if(changeListeners==null || changeListeners.isEmpty()) {
			return;
		}
		
		ChangeEvent event = new ChangeEvent(this);
		Object[] listeners = changeListeners.toArray();
		for(Object listener : listeners) {
			((ChangeListener)listener).stateChanged(event);
		}
	}
}
