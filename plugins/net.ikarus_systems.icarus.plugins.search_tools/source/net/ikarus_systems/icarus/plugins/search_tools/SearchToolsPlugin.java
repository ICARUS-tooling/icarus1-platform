/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools;

import java.util.logging.Level;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.search_tools.ConstraintContext;
import net.ikarus_systems.icarus.search_tools.SearchManager;
import net.ikarus_systems.icarus.util.ClassProxy;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchToolsPlugin extends Plugin {

	public SearchToolsPlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		registerConstraintFactories();
	}
	
	/**
	 * Load and register all content types that are defined at plug-in manifest level
	 */
	private void registerConstraintFactories() {
		for(Extension extension : getDescriptor().getExtensionPoint("ConstraintContext").getConnectedExtensions()) { //$NON-NLS-1$
			try {

				Extension.Parameter contentTypeParam = extension.getParameter("contentType"); //$NON-NLS-1$
				ContentType contentType = ContentTypeRegistry.getInstance().getType(contentTypeParam.valueAsExtension());
				
				ConstraintContext context = SearchManager.getInstance().getConstraintContext(contentType);
							
				for(Extension.Parameter tokenParam : extension.getParameters("token")) { //$NON-NLS-1$
					String token = tokenParam.valueAsString();
					try {
						context.addToken(token);
					}catch(Exception e) {
						LoggerFactory.log(this, Level.SEVERE, 
								"Failed to add token '"+token+"' in extension: "+extension.getUniqueId(), e); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
					for(Extension.Parameter aliasParam : tokenParam.getSubParameters("alias")) { //$NON-NLS-1$
						String alias = aliasParam.valueAsString();
						try {
							context.addAlias(alias, token);
						}catch(Exception e) {
							LoggerFactory.log(this, Level.SEVERE, 
									"Failed to add alias '"+alias+"' for token '"+token+"' in extension: "+extension.getUniqueId(), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
				
				ClassLoader loader = PluginUtil.getClassLoader(extension);
				
				for(Extension.Parameter factoryParam : extension.getParameter("factories").getSubParameters()) {  //$NON-NLS-1$
					String token = factoryParam.getId();
					String factoryClassName = factoryParam.rawValue();
					try {
						ClassProxy proxy = new ClassProxy(factoryClassName, loader);
						context.registerFactory(token, proxy);
					} catch(Exception e) {
						LoggerFactory.log(this, Level.SEVERE, 
								"Failed to register factory '"+factoryClassName+"' for token '"+token+" in extension: "+extension.getUniqueId(), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to register cosntraint context: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}

}
