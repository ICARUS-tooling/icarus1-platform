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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.prosody.ui.view;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyPerspective extends Perspective {

	public static final String PERSPECTIVE_ID = ProsodyConstants.PROSODY_PERSPECTIVE_ID;

	public ProsodyPerspective() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);


		focusView(ProsodyConstants.PAINTE_EDITOR_VIEW_ID);
	}

	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();

		String[] defaultViewIds = {
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
		};

		Set<Extension> newExtensions = new HashSet<>();

		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));

		// Collect all extensions that are connected to the ProsodyView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint prosodyViewPoint = descriptor.getExtensionPoint("ProsodyView"); //$NON-NLS-1$
		if(prosodyViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					prosodyViewPoint, true, true, null));
		}

		connectedViews.addAll(newExtensions);

		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED,
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}
}
