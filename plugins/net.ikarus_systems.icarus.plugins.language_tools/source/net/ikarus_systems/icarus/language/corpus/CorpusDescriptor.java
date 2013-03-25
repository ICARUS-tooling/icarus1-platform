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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.xml.ExtensionAdapter;
import net.ikarus_systems.icarus.xml.LocationAdapter;
import net.ikarus_systems.icarus.xml.MapAdapter;

import org.java.plugin.registry.Extension;


/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement(name="corpus", namespace="")
@XmlAccessorType(XmlAccessType.FIELD)
public class CorpusDescriptor implements Comparable<CorpusDescriptor> {
	
	// User defined id for the corpus
	@XmlElement(name="name")
	private String name;
	
	// Internal id
	@XmlAttribute(name="id")
	private String id;
	
	// Extension defining the corpus class
	@XmlElement
	@XmlJavaTypeAdapter(ExtensionAdapter.class)
	private Extension extension;
	
	// Corpus instance wrapped by this descriptor
	@XmlTransient
	private Corpus corpus;
	
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
	
	public Corpus getCorpus() {
		if(corpus==null)
			throw new IllegalStateException("No corpus loaded: "+id); //$NON-NLS-1$
		
		return corpus;
	}
	
	void instantiateCorpus() throws Exception {
		if(corpus!=null)
			throw new IllegalStateException("Corpus already instantiated: "+id); //$NON-NLS-1$

		ClassLoader loader = PluginUtil.getPluginManager().getPluginClassLoader(
				extension.getDeclaringPluginDescriptor());
		String className = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
		Class<?> clazz = loader.loadClass(className);
		
		corpus = (Corpus) clazz.newInstance();
		
		syncToCorpus();
	}
	
	public boolean isValid() {
		return corpus!=null;
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
	
	public void syncFromCorpus() {
		if(corpus==null)
			throw new IllegalStateException("No corpus loaded: "+id); //$NON-NLS-1$
		
		getProperties().clear();
		corpus.saveState(this);
	}
	
	public void syncToCorpus() {
		if(corpus==null)
			throw new IllegalStateException("No corpus loaded: "+id); //$NON-NLS-1$
		
		corpus.loadState(this);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CorpusDescriptor) {
			return id.equals(((CorpusDescriptor)obj).id);
		}
		return false;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CorpusDescriptor other) {
		return name.compareTo(other.name);
	}
}