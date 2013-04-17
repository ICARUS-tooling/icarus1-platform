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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CorpusInfo {
	
	@XmlAttribute
	private String pluginId;

	@XmlAttribute
	private String pluginVersion;

	@XmlValue
	private String corpusName;

	@XmlAttribute
	private String corpusClass;

	public CorpusInfo() {
		// no-op
	}

	public CorpusInfo(CorpusDescriptor descriptor) {
		Extension extension = descriptor.getExtension();
		PluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
		Corpus corpus = descriptor.getCorpus();
		
		pluginId = pluginDescriptor.getId();
		pluginVersion = pluginDescriptor.getVersion().toString();
		corpusName = corpus.getName();
		corpusClass = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
	}

	/**
	 * @return the pluginId
	 */
	public String getPluginId() {
		return pluginId;
	}

	/**
	 * @return the pluginVersion
	 */
	public String getPluginVersion() {
		return pluginVersion;
	}

	/**
	 * @return the corpusName
	 */
	public String getCorpusName() {
		return corpusName;
	}

	/**
	 * @return the corpusClass
	 */
	public String getCorpusClass() {
		return corpusClass;
	}
	
	@Override
	public String toString() {
		return corpusName+" ("+corpusClass+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String fullInfo() {
		return String.format("{name='%s' plugin=%s version=%s class=%s}",  //$NON-NLS-1$
				corpusName, pluginId, pluginVersion, corpusClass);
	}
}
