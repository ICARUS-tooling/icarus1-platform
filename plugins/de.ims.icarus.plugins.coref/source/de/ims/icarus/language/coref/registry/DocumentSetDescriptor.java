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
package de.ims.icarus.language.coref.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.java.plugin.registry.Extension;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.io.Reader;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Wrapper;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.data.DataList;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.xml.jaxb.ExtensionAdapter;
import de.ims.icarus.xml.jaxb.LocationAdapter;
import de.ims.icarus.xml.jaxb.MapAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="documentSet")
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentSetDescriptor implements Loadable, 
		Wrapper<CoreferenceDocumentSet>, DataList<AllocationDescriptor>, NamedObject {

	// User defined id for the document set
	@XmlElement(name="name", defaultValue="<unnamed>")
	private String name;
	
	// Internal id
	@XmlID
	@XmlAttribute(name="id")
	private String id;

	// Location
	@XmlElement(name="location")
	@XmlJavaTypeAdapter(LocationAdapter.class)
	private Location location;
	
	@XmlElement(name="properties", required=false)
	@XmlJavaTypeAdapter(MapAdapter.class)
	private Map<String, Object> properties;
	
	@XmlTransient
	private CoreferenceDocumentSet documentSet;
		
	@XmlElement(name="reader")
	@XmlJavaTypeAdapter(ExtensionAdapter.class)
	private Extension readerExtension;
	
	@XmlElement(name="allocations")
	private List<AllocationDescriptor> allocations = new ArrayList<>();
	
	@XmlTransient
	private AtomicBoolean loading = new AtomicBoolean();
	
	@XmlTransient
	private EventListenerList listeners;

	public DocumentSetDescriptor() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return getDocumentSet().size()>0;
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoading()
	 */
	@Override
	public boolean isLoading() {
		return loading.get();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#load()
	 */
	@SuppressWarnings("resource")
	@Override
	public void load() throws Exception {
		if(!loading.compareAndSet(false, true))
			throw new IllegalStateException("Loading process already started"); //$NON-NLS-1$

		Reader<CoreferenceDocumentData> reader = null;
		
		try {

			reader = createReader();
			if(reader==null)
				throw new IllegalStateException("No valid reader available"); //$NON-NLS-1$
			
			Options options = new Options(getProperties());
			CoreferenceDocumentSet documentSet = getDocumentSet();
			documentSet.free();
			
			CoreferenceUtils.loadDocumentSet(reader, getLocation(), options, documentSet);
		} finally {
			
			if(reader!=null) {
				try {
					reader.close();
				} catch(IOException e) {
					LoggerFactory.error(this, "Failed to close reader", e); //$NON-NLS-1$
				}
			}
			
			loading.set(false);
		}
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Location getLocation() {
		return location;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public CoreferenceDocumentSet getDocumentSet() {
		if(documentSet==null) {
			documentSet = new CoreferenceDocumentSet();
		}
		return documentSet;
	}

	@SuppressWarnings("unchecked")
	private Reader<CoreferenceDocumentData> createReader() {
		Extension readerExtension = getReaderExtension();
		if(readerExtension==null) {
			return null;
		}
		try {
			return (Reader<CoreferenceDocumentData>) PluginUtil.instantiate(readerExtension);
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to instantiate reader: "+readerExtension.getUniqueId(), e); //$NON-NLS-1$
		}
		return null;
	}

	public Extension getReaderExtension() {
		return readerExtension;
	}

	public void setName(String name) {
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$
		if(name.equals(this.name)) {
			return;
		}
		
		this.name = name;
	}

	public void setId(String id) {
		if(id==null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if(id.equals(this.id)) {
			return;
		}
		
		this.id = id;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setReaderExtension(Extension readerExtension) {
		if(readerExtension!=null && readerExtension.equals(this.readerExtension)) {
			return;
		}
		
		this.readerExtension = readerExtension;
		free();
	}

	public void free() {
		CoreferenceDocumentSet documentSet = getDocumentSet();
		
		documentSet.free();
		
		fireChangeEvent();
	}

	/**
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public CoreferenceDocumentSet get() {
		return getDocumentSet();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#size()
	 */
	@Override
	public int size() {
		return allocations.size();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#get(int)
	 */
	@Override
	public AllocationDescriptor get(int index) {
		return allocations.get(index);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return getEntryType();
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener listener) {
		if (listeners==null) {
			listeners = new EventListenerList();
		}
		
		listeners.add(ChangeListener.class, listener);
	}

	/**
	 * @see de.ims.icarus.util.data.DataList#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener listener) {
		if(listeners==null) {
			return;
		}
		
		listeners.remove(ChangeListener.class, listener);
	}
	
	private void fireChangeEvent() {
		if(listeners==null) {
			return;
		}
		
		ChangeListener[] changeListeners = listeners.getListeners(ChangeListener.class);
		if(changeListeners==null) {
			return;
		}
		ChangeEvent event = new ChangeEvent(this);
		for(ChangeListener listener : changeListeners) {
			listener.stateChanged(event);
		}
	}
	
	public void addAllocation(AllocationDescriptor allocation) {
		if(allocation==null)
			throw new NullPointerException("Invalid allocation"); //$NON-NLS-1$
		
		allocation.setParent(this);
		allocations.add(allocation);
		
		fireChangeEvent();
	}
	
	public void removeAllocation(int index) {
		if(allocations.isEmpty()) {
			return;
		}
		
		AllocationDescriptor allocation = allocations.remove(index);
		allocation.setParent(null);
		
		fireChangeEvent();
	}
	
	public int removeAllocation(AllocationDescriptor allocation) {
		if(allocation==null)
			throw new NullPointerException("Invalid allocation"); //$NON-NLS-1$
		
		if(allocations.isEmpty()) {
			return -1;
		}
		
		int index = allocations.indexOf(allocation);
		
		if(index==-1)
			throw new IllegalArgumentException("Allocation not found in list: "+allocation.getName()); //$NON-NLS-1$
		
		allocations.remove(index);
		allocation.setParent(null);
		
		fireChangeEvent();
		
		return index;
	}
	
	public int indexOfAllocation(AllocationDescriptor allocation) {
		return allocations.isEmpty() ? -1 : allocations.indexOf(allocation);
	}
	
	private static ContentType allocationDescriptorContentType;
	public static ContentType getEntryType() {
		if(allocationDescriptorContentType==null) {
			synchronized (DocumentSetDescriptor.class) {
				if(allocationDescriptorContentType==null) {
					ContentTypeRegistry.getInstance().addType(AllocationDescriptor.class);
				}
			}
		}
		
		return allocationDescriptorContentType;
	}
}
