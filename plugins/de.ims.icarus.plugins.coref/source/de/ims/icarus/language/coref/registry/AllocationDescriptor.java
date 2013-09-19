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

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.java.plugin.registry.Extension;

import de.ims.icarus.io.Loadable;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.io.AllocationReader;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.NamedObject;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.Wrapper;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.xml.jaxb.ExtensionAdapter;
import de.ims.icarus.xml.jaxb.LocationAdapter;
import de.ims.icarus.xml.jaxb.MapAdapter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="allocation")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllocationDescriptor implements Loadable, 
		Wrapper<CoreferenceAllocation>, NamedObject {

	// User defined id for the allocation
	@XmlElement(name="name")
	private String name;

	// Internal id
	@XmlID
	@XmlAttribute(name="id")
	private String id;

	// Location
	@XmlElement(name="location")
	@XmlJavaTypeAdapter(LocationAdapter.class)
	private Location location;
	
	@XmlTransient
	private CoreferenceAllocation allocation;
	
	@XmlElement(name="properties", required=false)
	@XmlJavaTypeAdapter(MapAdapter.class)
	private Map<String, Object> properties;

	@XmlElement(name="reader")
	@XmlJavaTypeAdapter(ExtensionAdapter.class)
	private Extension readerExtension;

	@XmlTransient
	private AtomicBoolean loading = new AtomicBoolean();

	@XmlIDREF
	@XmlElement(name="parent")
	private DocumentSetDescriptor parent;
	
	protected AllocationDescriptor() {
		// no-op
	}
	
	public AllocationDescriptor(DocumentSetDescriptor parent) {
		if(parent==null)
			throw new IllegalArgumentException("Invaldi parent"); //$NON-NLS-1$
		
		setParent(parent);
	}
	
	public DocumentSetDescriptor getParent() {
		return parent;
	}
	
	void setParent(DocumentSetDescriptor parent) {
		this.parent = parent;
	}

	/**
	 * @see de.ims.icarus.util.Wrapper#get()
	 */
	@Override
	public CoreferenceAllocation get() {
		return getAllocation();
	}

	/**
	 * @see de.ims.icarus.io.Loadable#isLoaded()
	 */
	@Override
	public boolean isLoaded() {
		return getAllocation().size()>0;
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
	@Override
	public void load() throws Exception {
		if(!loading.compareAndSet(false, true))
			throw new IllegalStateException("Loading process already started"); //$NON-NLS-1$
		
		try {

			AllocationReader reader = createReader();
			if(reader==null)
				throw new IllegalStateException("No valid reader available"); //$NON-NLS-1$
			
			Options options = new Options(getProperties());
			CoreferenceAllocation allocation = getAllocation();
			allocation.free();
			
			DocumentSetDescriptor parent = getParent();
			if(parent==null)
				throw new IllegalStateException("No parent set"); //$NON-NLS-1$
			
			reader.init(getLocation(), options, parent.getDocumentSet());
			
			reader.readAllocation(allocation);
		} finally {
			loading.set(false);
		}
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	private AllocationReader createReader() {
		Extension readerExtension = getReaderExtension();
		try {
			return (AllocationReader) PluginUtil.instantiate(readerExtension);
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to instantiate allocation reader: "+readerExtension.getUniqueId(), e); //$NON-NLS-1$
		}
		return null;
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
	
	protected void setAllocation(CoreferenceAllocation allocation) {
		this.allocation = allocation;
	}

	public CoreferenceAllocation getAllocation() {
		if(allocation==null) {
			allocation = new CoreferenceAllocation();
		}
		return allocation;
	}

	public Extension getReaderExtension() {
		return readerExtension;
	}

	public void setName(String name) {
		if(name==null)
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
		if(name.equals(this.name)) {
			return;
		}
		
		this.name = name;
	}

	public void setId(String id) {
		if(id==null)
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
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
		
	}
}