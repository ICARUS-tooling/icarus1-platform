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
package de.ims.icarus.plugins.coref;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JComponent;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.Core;
import de.ims.icarus.language.coref.registry.CoreferenceRegistry;
import de.ims.icarus.language.coref.registry.DocumentSetDescriptor;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.IcarusCorePlugin;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.location.Locations;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferencePerspective extends Perspective {

	public static final String PERSPECTIVE_ID = CorefConstants.COREFERENCE_PERSPECTIVE_ID;

	public static final String EXAMPLE_DOCUMENT_SET_NAME = "ICARUS example document set"; //$NON-NLS-1$

	public CoreferencePerspective() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {

		ensureExampleDocumentSet();

		collectViewExtensions();
		defaultDoLayout(container);

		focusView(CorefConstants.COREFERENCE_MANAGER_VIEW_ID);
	}

	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();

		String[] defaultViewIds = {
				CorefConstants.COREFERENCE_MANAGER_VIEW_ID,
				CorefConstants.COREFERENCE_EXPLORER_VIEW_ID,
				CorefConstants.COREFERENCE_DOCUMENT_VIEW_ID,
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
				/*ManagementConstants.TABLE_VIEW_ID,*/
		};

		Set<Extension> newExtensions = new HashSet<>();

		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));

		// Collect all extensions that are connected to the CoreferenceView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("CoreferenceView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					managementViewPoint, true, true, null));
		}

		connectedViews.addAll(newExtensions);

		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED,
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

	public static void ensureExampleDocumentSet() {

		// Ensure some default document set files if required

		if(IcarusCorePlugin.isShowExampleData()) {
			ClassLoader loader = CoreferencePlugin.class.getClassLoader();
			String[] resources = {
				"icarus.conll09", //$NON-NLS-1$
			};
			String root = "de/ims/icarus/plugins/coref/resources/"; //$NON-NLS-1$
			String folder = "coref"; //$NON-NLS-1$

			for(String resource : resources) {
				String path = root+resource;
				Core.getCore().ensureResource(folder, resource, path, loader);
			}

			String name = EXAMPLE_DOCUMENT_SET_NAME;

			CoreferenceRegistry registry = CoreferenceRegistry.getInstance();

			DocumentSetDescriptor doc = registry.getDocumentSetByName(name);

			if(doc==null) {
				String path = "data/coref/icarus.conll09"; //$NON-NLS-1$

				try {
					doc = registry.newDocumentSet(name);
					doc.setLocation(Locations.getFileLocation(path));
					doc.setReaderExtension(PluginUtil.getExtension("de.ims.icarus.coref@CONLL12DocumentReader")); //$NON-NLS-1$
				} catch (Exception e) {
					LoggerFactory.log(CoreferencePlugin.class, Level.SEVERE,
							"Failed to generate and register example document set: "+name, e); //$NON-NLS-1$
					return;
				}
			}

			//TODO add 1 or 2 allocation files
		}
	}
}
