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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;

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
