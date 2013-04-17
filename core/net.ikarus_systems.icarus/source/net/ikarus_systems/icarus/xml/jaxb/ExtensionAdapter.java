/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.registry.Extension;

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