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

import java.util.Collection;

import org.java.plugin.Plugin;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

import de.ims.icarus.plugins.PluginUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferencePlugin extends Plugin {

	public CoreferencePlugin() {
		// no-op
	}

	/**
	 * @see org.java.plugin.Plugin#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.java.plugin.Plugin#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// TODO Auto-generated method stub

	}
	
	public static Collection<Extension> getDocumentReaderExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				CorefConstants.COREFERENCE_PLUGIN_ID, "DocumentReader"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}
	
	public static Collection<Extension> getAllocationReaderExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				CorefConstants.COREFERENCE_PLUGIN_ID, "AllocationReader"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}
	
	public static Collection<Extension> getCoreferencePresenterExtensions() {
		ExtensionPoint extensionPoint = PluginUtil.getPluginRegistry().getExtensionPoint(
				CorefConstants.COREFERENCE_PLUGIN_ID, "CoreferencePresenter"); //$NON-NLS-1$
		return extensionPoint.getConnectedExtensions();
	}
}
