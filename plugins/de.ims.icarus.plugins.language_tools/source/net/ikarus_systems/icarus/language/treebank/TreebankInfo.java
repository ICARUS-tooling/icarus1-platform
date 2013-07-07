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
public class TreebankInfo {
	
	@XmlAttribute
	private String pluginId;

	@XmlAttribute
	private String pluginVersion;

	@XmlValue
	private String treebankName;

	@XmlAttribute
	private String treebankClass;

	public TreebankInfo() {
		// no-op
	}

	public TreebankInfo(TreebankDescriptor descriptor) {
		Extension extension = descriptor.getExtension();
		PluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
		Treebank treebank = descriptor.getTreebank();
		
		pluginId = pluginDescriptor.getId();
		pluginVersion = pluginDescriptor.getVersion().toString();
		treebankName = treebank.getName();
		treebankClass = extension.getParameter("class").valueAsString(); //$NON-NLS-1$
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
	 * @return the treebankName
	 */
	public String getTreebankName() {
		return treebankName;
	}

	/**
	 * @return the treebankClass
	 */
	public String getTreebankClass() {
		return treebankClass;
	}
	
	@Override
	public String toString() {
		return treebankName+" ("+treebankClass+")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public String fullInfo() {
		return String.format("{name='%s' plugin=%s version=%s class=%s}",  //$NON-NLS-1$
				treebankName, pluginId, pluginVersion, treebankClass);
	}
}
