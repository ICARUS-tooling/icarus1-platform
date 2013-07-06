/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/net.ikarus_systems.icarus/source/net/ikarus_systems/icarus/xml/jaxb/ExtensionAdapter.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.xml.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.plugins.PluginUtil;

import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id: ExtensionAdapter.java 23 2013-04-17 12:39:04Z mcgaerty $
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