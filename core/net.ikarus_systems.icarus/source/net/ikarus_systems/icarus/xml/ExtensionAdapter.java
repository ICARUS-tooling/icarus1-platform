/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ExtensionAdapter extends XmlAdapter<String, Extension> {
	public Extension unmarshal(String guid) {
		String[] parts = guid.split("\\@"); //$NON-NLS-1$
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(parts[0]);
		Extension extension = descriptor.getExtension(parts[1]);
		
		return extension;
	}

	public String marshal(Extension extension) {
		return extension.getUniqueId();
	}
}