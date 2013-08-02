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
package de.ims.icarus.plugins.search_tools;

import java.util.logging.Level;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.ClassProxy;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

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
		registerSearchOperators();
		registerConstraintFactories();
	}
	
	private void registerSearchOperators() {
		
		SearchOperator.register(DefaultSearchOperator.EQUALS);
		SearchOperator.register(DefaultSearchOperator.EQUALS_NOT);
		SearchOperator.register(DefaultSearchOperator.MATCHES);
		SearchOperator.register(DefaultSearchOperator.MATCHES_NOT);
		SearchOperator.register(DefaultSearchOperator.CONTAINS);
		SearchOperator.register(DefaultSearchOperator.CONTAINS_NOT);
		SearchOperator.register(DefaultSearchOperator.LESS_THAN);
		SearchOperator.register(DefaultSearchOperator.LESS_OR_EQUAL);
		SearchOperator.register(DefaultSearchOperator.GREATER_THAN);
		SearchOperator.register(DefaultSearchOperator.GREATER_OR_EQUAL);
		SearchOperator.register(DefaultSearchOperator.GROUPING);

		for(Extension extension : getDescriptor().getExtensionPoint("SearchOperator").getConnectedExtensions()) { //$NON-NLS-1$
			try {
				SearchOperator operator = (SearchOperator)PluginUtil.instantiate(extension);
				SearchOperator.register(operator);
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to register search-operator: "+extension.getUniqueId(), e); //$NON-NLS-1$
			}
		}
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
				
				PluginUtil.activatePlugin(extension);
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
						"Failed to register constraint context: "+extension.getUniqueId(), e); //$NON-NLS-1$
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
