/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools;

import java.util.HashMap;
import java.util.Map;

import net.ikarus_systems.icarus.language.tokenizer.Tokenizer;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.util.id.ExtensionIdentity;
import net.ikarus_systems.icarus.util.id.Identity;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class Tokenizers {
	
	private static Map<Extension, Identity> tokenizerIdentities;
	
	private Map<Extension, Tokenizer> loadedTokenizers;

	/**
	 * 
	 */
	public Tokenizers() {
		// TODO Auto-generated constructor stub
	}

	
	private static Identity getTokenizerIdentity(Extension extension) {
		if(tokenizerIdentities==null) {
			tokenizerIdentities = new HashMap<>();
			
			PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(
					LanguageToolsPlugin.PLUGIN_ID);
			for(Extension tokenizerExt : descriptor.getExtensionPoint("Tokenizer") //$NON-NLS-1$
					.getConnectedExtensions()) {
				tokenizerIdentities.put(tokenizerExt, new ExtensionIdentity(tokenizerExt));
			}
		}
		
		return tokenizerIdentities.get(extension);
	}
}
