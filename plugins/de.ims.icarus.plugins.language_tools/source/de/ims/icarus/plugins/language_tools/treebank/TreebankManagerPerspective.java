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
package de.ims.icarus.plugins.language_tools.treebank;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JComponent;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.Core;
import de.ims.icarus.language.treebank.TreebankDescriptor;
import de.ims.icarus.language.treebank.TreebankRegistry;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.IcarusCorePlugin;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.plugins.language_tools.LanguageToolsPlugin;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankManagerPerspective extends Perspective {
	
	public static final String PERSPECTIVE_ID = LanguageToolsConstants.TREEBANK_MANAGER_PERSPECTIVE_ID;

	public static final String EXAMPLE_TREEBANK_NAME = "ICARUS example treebank"; //$NON-NLS-1$
	
	
	public TreebankManagerPerspective() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		ensureExampleTreebank();
		
		collectViewExtensions();
		defaultDoLayout(container);
		
		focusView(LanguageToolsConstants.TREEBANK_EXPLORER_VIEW_ID);
	}
	
	public static void ensureExampleTreebank() {
		
		// Ensure some default treebank files if required
		
		if(IcarusCorePlugin.isShowExampleData()) {
			ClassLoader loader = LanguageToolsPlugin.class.getClassLoader();
			String[] resources = {
				"icarus.conll09", //$NON-NLS-1$
			};
			String root = "de/ims/icarus/plugins/language_tools/resources/"; //$NON-NLS-1$
			String folder = "treebanks"; //$NON-NLS-1$
			
			for(String resource : resources) {
				String path = root+resource;
				Core.getCore().ensureResource(folder, resource, path, loader);
			}
			
			String name = EXAMPLE_TREEBANK_NAME;
			
			TreebankRegistry registry = TreebankRegistry.getInstance();
			
			if(registry.getDescriptorByName(name)==null) {
				String path = "data/treebanks/icarus.conll09"; //$NON-NLS-1$
				
				try {
					TreebankDescriptor td = registry.newTreebank("DefaultSimpleTreebank", name); //$NON-NLS-1$
					td.setLocation(Locations.getFileLocation(path));
					td.getProperties().put(DefaultSimpleTreebank.READER_EXTENSION_PROPERTY,
							"de.ims.icarus.matetools@CONLL09SentenceDataPredictedReader"); //$NON-NLS-1$
					
					td.syncToTreebank();
				} catch (Exception e) {
					LoggerFactory.log(TreebankManagerPerspective.class, Level.SEVERE, 
							"Failed to generate and register example treebank: "+name, e); //$NON-NLS-1$
				}
			}
		}
	}
	
	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();
		
		String[] defaultViewIds = {
				LanguageToolsConstants.TREEBANK_EXPLORER_VIEW_ID,
				//LanguageToolsConstants.TREEBANK_EDIT_VIEW_ID,
				LanguageToolsConstants.TREEBANK_PROPERTIES_VIEW_ID,
				/*LanguageToolsConstants.TREEBANK_INSPECT_VIEW_ID,*/
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
				ManagementConstants.TABLE_VIEW_ID,
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));
		
		// Collect all extensions that are connected to the TreebankManagementView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("TreebankManagementView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					managementViewPoint, true, true, null));
		}
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}
}
