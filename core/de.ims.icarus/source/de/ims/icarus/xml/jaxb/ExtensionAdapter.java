/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;


import org.java.plugin.registry.Extension;

import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionAdapter extends XmlAdapter<String, Extension> {
	public Extension unmarshal(String uid) {
		return PluginUtil.getExtension(uid);
	}

	public String marshal(Extension extension) {
		return extension.getUniqueId();
	}
}