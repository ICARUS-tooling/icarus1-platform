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

import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.java.plugin.registry.Extension;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.xml.jaxb.ExtensionAdapter;
import de.ims.icarus.xml.jaxb.LocationAdapter;
import de.ims.icarus.xml.jaxb.MapAdapter;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="treebank")
@XmlAccessorType(XmlAccessType.FIELD)
public class TreebankDescriptor implements Comparable<TreebankDescriptor> {
	
	// User defined id for the treebank
	@XmlElement(name="name")
	private String name;
	
	// Internal id
	@XmlAttribute(name="id")
	private String id;
	
	// Extension defining the treebank class
	@XmlElement
	@XmlJavaTypeAdapter(ExtensionAdapter.class)
	private Extension extension;
	
	// Treebank instance wrapped by this descriptor
	@XmlTransient
	private Treebank treebank;
	
	// Location
	@XmlElement(name="location")
	@XmlJavaTypeAdapter(LocationAdapter.class)
	private Location location;
	
	@XmlElement(name="properties", required=false)
	@XmlJavaTypeAdapter(MapAdapter.class)
	private Map<String, Object> properties;
	
	public Extension getExtension() {
		return extension;
	}
	
	void setExtension(Extension extension) {
		this.extension = extension;
	}
	
	boolean hasTreebank() {
		return treebank!=null;
	}
	
	public Treebank getTreebank() {
		if(treebank==null)
			throw new IllegalStateException("No treebank loaded: "+id); //$NON-NLS-1$
		
		return treebank;
	}
	
	void instantiateTreebank() throws Exception {
		if(treebank!=null)
			throw new IllegalStateException("Treebank already instantiated: "+id); //$NON-NLS-1$

		treebank = (Treebank) PluginUtil.instantiate(extension);
		
		syncToTreebank();
	}
	
	public boolean isValid() {
		return treebank!=null;
	}

	public String getName() {
		return name;
	}
	
	void setId(String id) {
		if(id==null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	@XmlTransient
	public Location getLocation() {
		return location;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if(name==null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$
		if(name.equals(this.name)) {
			return;
		}
		
		this.name = name;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public void setProperties(Map<String, Object> values) {
		if(values==null) {
			return;
		}
		getProperties().putAll(values);
	}
	
	public Map<String, Object> getProperties() {
		if(properties==null) {
			properties = new HashMap<>();
		}
		return properties;
	}
	
	public void syncFromTreebank() {
		if(treebank==null)
			throw new IllegalStateException("No treebank loaded: "+id); //$NON-NLS-1$
		
		getProperties().clear();
		treebank.saveState(this);
	}
	
	public void syncToTreebank() {
		if(treebank==null)
			throw new IllegalStateException("No treebank loaded: "+id); //$NON-NLS-1$
		
		treebank.loadState(this);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TreebankDescriptor) {
			return id.equals(((TreebankDescriptor)obj).id);
		}
		return false;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TreebankDescriptor other) {
		return name.compareTo(other.name);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}
}