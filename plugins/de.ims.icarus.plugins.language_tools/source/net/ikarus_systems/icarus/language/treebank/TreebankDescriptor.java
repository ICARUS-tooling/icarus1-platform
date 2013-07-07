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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.xml.jaxb.ExtensionAdapter;
import net.ikarus_systems.icarus.xml.jaxb.LocationAdapter;
import net.ikarus_systems.icarus.xml.jaxb.MapAdapter;

import org.java.plugin.registry.Extension;


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
	
	// 
	@XmlElement(name="location")
	@XmlJavaTypeAdapter(LocationAdapter.class)
	private Location location;
	
	@XmlElement(name="properties", nillable=true)
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

		ClassLoader loader = PluginUtil.getClassLoader(extension);
		String className = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
		Class<?> clazz = loader.loadClass(className);
		
		treebank = (Treebank) clazz.newInstance();
		
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
			throw new IllegalArgumentException("Invalid id"); //$NON-NLS-1$
		
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
			throw new IllegalArgumentException("Invalid name"); //$NON-NLS-1$
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
}