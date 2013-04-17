/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class GrammarRegistry {
	
	private static GrammarRegistry instance;
	
	public static GrammarRegistry getInstance() {
		if(instance==null) {
			synchronized (GrammarRegistry.class) {
				if(instance==null) {
					instance = new GrammarRegistry();
				}
			}
		}
		
		return instance;
	}
	
	private Map<String, Grammar> grammars;

	private GrammarRegistry() {
		grammars = new HashMap<>();
		
		PluginDescriptor descriptor = PluginUtil.getPluginRegistry().getPluginDescriptor(
				LanguageToolsConstants.LANGUAGE_TOOLS_PLUGIN_ID);
		PluginManager pluginManager = PluginUtil.getPluginManager();
		for(Extension extension : descriptor.getExtensionPoint("Grammar").getConnectedExtensions()) { //$NON-NLS-1$
			ClassLoader loader = PluginUtil.getClassLoader(extension);
			Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
			try {
				Class<?> clazz = loader.loadClass(param.valueAsString());
				Grammar grammar = (Grammar) clazz.newInstance();
				
				// TODO check for duplicates? 
				grammars.put(grammar.getIdentifier(), grammar);
			} catch(Exception e) {
				LoggerFactory.getLogger(GrammarRegistry.class).log(LoggerFactory.record(Level.SEVERE, 
						"Failed to load grammar: "+extension.getUniqueId(), e)); //$NON-NLS-1$
			}
		}
	}

	public Grammar getGrammar(String id) {
		return grammars==null ? null : grammars.get(id);
	}
}
