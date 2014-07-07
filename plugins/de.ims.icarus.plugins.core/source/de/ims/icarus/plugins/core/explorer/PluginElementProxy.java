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
package de.ims.icarus.plugins.core.explorer;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Identity;
import org.java.plugin.registry.Library;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginFragment;

import de.ims.icarus.io.IOUtil;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.resources.DefaultResourceLoader;
import de.ims.icarus.resources.ManagedResource;
import de.ims.icarus.resources.ResourceLoader;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.CompoundIcon;
import de.ims.icarus.ui.DecoratedIcon;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.Wrapper;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PluginElementProxy implements Wrapper<Object> {

	private final Object element, parent;

	private ManagedResource resource;
	private String nameKey;
	private String descriptionKey;
	private String label;
	private Icon icon;

	public PluginElementProxy(Object element) {
		this(element, null);
	}

	/**
	 * @param element
	 */
	public PluginElementProxy(Object element, Object parent) {
		Exceptions.testNullArgument(element, "element"); //$NON-NLS-1$

		this.element = element;
		this.parent = parent;

		refresh();
	}

	public void refresh() {

		PluginManager pluginManager = PluginUtil.getPluginManager();

		if(element instanceof Extension) {
			// EXTENSION

			Extension extension = (Extension) element;
			@SuppressWarnings("resource")
			ClassLoader classLoader = pluginManager.getPluginClassLoader(extension.getDeclaringPluginDescriptor());
			Extension.Parameter param = extension.getParameter("resource"); //$NON-NLS-1$
			if(param!=null) {
				String basename = param.valueAsString();
				ResourceLoader loader = new DefaultResourceLoader(classLoader);
				resource = ResourceManager.getInstance().addManagedResource(basename, loader);
			}

			param = extension.getParameter("name"); //$NON-NLS-1$
			if(param!=null)
				nameKey = param.valueAsString();

			param = extension.getParameter("description"); //$NON-NLS-1$
			if(param!=null)
				descriptionKey = param.valueAsString();

			// try to load icon
			param=extension.getParameter("icon"); //$NON-NLS-1$
			if(param!=null) {
				// we use the declaring plug-in's class loader to fetch the resource
				URL iconLocation = classLoader.getResource(param.valueAsString());
				if(iconLocation==null) {
					// inform developer of missing icon resource
					LoggerFactory.log(this, Level.WARNING, "Failed to load icon '"+param.valueAsString()+"' for extension "+extension.getUniqueId()); //$NON-NLS-1$ //$NON-NLS-2$
				} else
					icon = new ImageIcon(iconLocation);
			}

			// ensure we show at least the 'default' extension-object icon
			if(icon==null)
				icon = IconRegistry.getGlobalRegistry().getIcon("extension_obj.gif"); //$NON-NLS-1$

		} else if(element instanceof PluginDescriptor) {
			// PLUGIN DESCRIPTOR

			PluginDescriptor descriptor = (PluginDescriptor) element;
			CompoundIcon descIcon = new CompoundIcon(UIUtil.getBlankIcon(24, 16));
			descIcon.setTopLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("plugin_obj.gif")); //$NON-NLS-1$
			if(pluginManager.isBadPlugin(descriptor))
				descIcon.setBottomRightOverlay(IconRegistry.getGlobalRegistry().getIcon("error_co.gif")); //$NON-NLS-1$
			else if(pluginManager.isPluginActivated(descriptor))
				descIcon.setBottomRightOverlay(IconRegistry.getGlobalRegistry().getIcon("task_in_progress.gif")); //$NON-NLS-1$

			icon = descIcon;

		} else if(element instanceof PluginFragment) {
			// PLUGIN FRAGMENT

			icon = IconRegistry.getGlobalRegistry().getIcon("frgmt_obj.gif"); //$NON-NLS-1$

		} else if(element instanceof ExtensionPoint) {
			// EXTENSION POINT

			DecoratedIcon extPointIcon = new DecoratedIcon(UIUtil.getBlankIcon(42, 16));
			extPointIcon.addBackgroundDecoration(
					IconRegistry.getGlobalRegistry().getIcon("ext_point_obj.gif"), 0, 0); //$NON-NLS-1$

			ExtensionPoint extensionPoint = (ExtensionPoint) element;
			switch (extensionPoint.getMultiplicity()) {
			case NONE:
				// Mark abstract extension points
				extPointIcon.addOverlayDecoration(
						IconRegistry.getGlobalRegistry().getIcon("abstract_co.gif"), 16, 0); //$NON-NLS-1$
				break;

			case ANY:
				extPointIcon.addOverlayDecoration(
						IconRegistry.getGlobalRegistry().getIcon("EOccurrenceZeroToN.gif"), 24, 0); //$NON-NLS-1$
				break;

			case ONE:
				extPointIcon.addOverlayDecoration(
						IconRegistry.getGlobalRegistry().getIcon("EOccurrenceOne.gif"), 24, 0); //$NON-NLS-1$
				break;

			case ONE_PER_PLUGIN:
				extPointIcon.addOverlayDecoration(
						IconRegistry.getGlobalRegistry().getIcon("stcksync_ov.gif"), 24, 8); //$NON-NLS-1$
				break;
			}

			// Mark derived extension points
			if(extensionPoint.getParentExtensionPointId()!=null) {
				extPointIcon.addOverlayDecoration(
						IconRegistry.getGlobalRegistry().getIcon("over_co.gif"), 16, 8); //$NON-NLS-1$
			}

			// Mark invalid extension points
			if(!extensionPoint.isValid()) {
				extPointIcon.addOverlayDecoration(
						IconRegistry.getGlobalRegistry().getIcon("error_co.gif"), 0, 8); //$NON-NLS-1$
			}

			icon = extPointIcon;

		} else if(element instanceof ExtensionPoint.ParameterDefinition) {
			// PARAMETER DEFINITION

			ExtensionPoint.ParameterDefinition def = (ExtensionPoint.ParameterDefinition)element;

			label = def.getId()+" ("+def.getType().toCode()+")"; //$NON-NLS-1$ //$NON-NLS-2$
			CompoundIcon defIcon = new CompoundIcon(UIUtil.getBlankIcon(24, 16));

			// Mark derived extension points
			ExtensionPoint extensionPoint = def.getDeclaringExtensionPoint();
			if(parent instanceof ExtensionPoint && parent!=extensionPoint) {
				defIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry()
						.getIcon("implm_co.gif")); //$NON-NLS-1$
			}

			switch (def.getMultiplicity()) {
			case ANY:
				defIcon.setBottomRightOverlay(IconRegistry.getGlobalRegistry()
						.getIcon("EOccurrenceZeroToN.gif")); //$NON-NLS-1$
				break;

			case NONE_OR_ONE:
				defIcon.setBottomRightOverlay(IconRegistry.getGlobalRegistry()
						.getIcon("EOccurrenceZeroToOne.gif")); //$NON-NLS-1$
				break;

			case ONE:
				defIcon.setBottomRightOverlay(IconRegistry.getGlobalRegistry()
						.getIcon("EOccurrenceOne.gif")); //$NON-NLS-1$
				break;

			case ONE_OR_MORE:
				defIcon.setBottomRightOverlay(IconRegistry.getGlobalRegistry()
						.getIcon("EOccurrenceOneToN.gif")); //$NON-NLS-1$
				break;
			}

			icon = defIcon;

		} else if(element instanceof Extension.Parameter) {
			// PARAMETER

			Extension.Parameter param = (Extension.Parameter) element;

			String value = param.rawValue();

			label = param.getId()+": "+(value==null ? "<empty>" : value); //$NON-NLS-1$ //$NON-NLS-2$
		} else if(element instanceof Library) {
			// LIBRARY

			Library library = (Library) element;

			// Resolve library location
			URL libLocation = PluginUtil.getLocation(library);
			boolean libExists = IOUtil.isResourceExists(libLocation);
			boolean local = IOUtil.isLocalFile(libLocation);
			Path libFile = null;
			try {
				libFile = local ? Paths.get(libLocation.toURI()) : null;
			} catch (URISyntaxException e) {
				// ignore
			}

			String baseIconName = null;
			if(!libExists) {
				baseIconName = "disabled_co.gif"; //$NON-NLS-1$
			} else if(!local) {
				// Not a local library
				baseIconName = "url.gif"; //$NON-NLS-1$
			} else if(Files.isDirectory(libFile)) {
				// Resource folder
				baseIconName = "fldr_obj.gif"; //$NON-NLS-1$
			} else if(libFile.endsWith(".jar")) { //$NON-NLS-1$
				// Existing local jar file
				baseIconName = "jar_obj.gif"; //$NON-NLS-1$
			} else {
				// Just a single file?
				baseIconName = "file_obj.gif"; //$NON-NLS-1$
			}

			// Load icon and apply overlays
			CompoundIcon libIcon = new CompoundIcon(IconRegistry.getGlobalRegistry().getIcon(baseIconName));
			// Only possible if no integrity check is done
			// during launch (possible in dev-mode)
			if(!libExists)
				libIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, IconRegistry.getGlobalRegistry().getIcon("error_co.gif")); //$NON-NLS-1$

			if(library.isCodeLibrary())
				libIcon.setOverlay(CompoundIcon.BOTTOM_RIGHT, IconRegistry.getGlobalRegistry().getIcon("java_co.gif")); //$NON-NLS-1$

			icon = libIcon;
			label = library.getId()+": "+library.getPath(); //$NON-NLS-1$
		}
	}

	@Override
	public Object get() {
		return element;
	}

	public String getElementId() {
		return (element instanceof Identity) ? ((Identity)element).getId() : element.toString();
	}

	public PluginDescriptor getDescriptor() {
		if(element instanceof PluginElement)
			return ((PluginElement<?>)element).getDeclaringPluginDescriptor();
		else if(element instanceof PluginDescriptor)
			return (PluginDescriptor) element;
		else
			return null;
	}

	public String getElementLabel() {
		String id = label==null ? getElementId() : label;

		if(nameKey!=null && resource!=null) {
			return resource.getResource(nameKey)+" ("+id+")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return id;
	}

	public String getElementDescription() {
		if(descriptionKey!=null && resource!=null)
			return resource.getResource(descriptionKey);
		else
			return null;
	}

	@Override
	public String toString() {
		return getElementLabel();
	}

	public Icon getIcon() {
		return icon;
	}
}
