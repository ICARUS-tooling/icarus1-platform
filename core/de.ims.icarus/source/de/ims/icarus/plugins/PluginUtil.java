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
package de.ims.icarus.plugins;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PathResolver;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginAttribute;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginElement;
import org.java.plugin.registry.PluginRegistry;

import de.ims.icarus.Core;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.DefaultResourceLoader;
import de.ims.icarus.resources.ResourceLoader;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.dialog.DialogFactory;
import de.ims.icarus.util.Capability;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.MutablePrimitives.MutableBoolean;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.ExtensionIdentity;
import de.ims.icarus.util.id.Identity;
import de.ims.icarus.util.id.StaticIdentity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class PluginUtil {
	
	public static final String CORE_PLUGIN_ID = Core.CORE_PLUGIN_KEY;
	
	public static final String PREFERENCES_KEY = 
			"de.ims.icarus.preferences"; //$NON-NLS-1$
	
	public static final String ICONS_KEY = 
			"de.ims.icarus.icons"; //$NON-NLS-1$
	
	public static final String RESOURCES_KEY = 
			"de.ims.icarus.resources"; //$NON-NLS-1$
	
	private static PluginRegistry pluginRegistry;
	private static PathResolver pathResolver;
	private static PluginManager pluginManager;

	private PluginUtil() {
		// no-op
	}

	public static int countActive() {
		int active = 0;
		
		for(PluginDescriptor descriptor : getPluginRegistry().getPluginDescriptors()) {
			if(getPluginManager().isPluginActivated(descriptor))
				active++;
		}
		
		return active;
	}
	
	public static int countEnabled() {
		int active = 0;
		
		for(PluginDescriptor descriptor : getPluginRegistry().getPluginDescriptors()) {
			if(getPluginManager().isPluginEnabled(descriptor))
				active++;
		}
		
		return active;
	}
	
	public static int pluginCount() {
		return getPluginRegistry().getPluginDescriptors().size();
	}
	
	// prevent multiple deserialization
	private Object readResolve() throws ObjectStreamException {
		throw new NotSerializableException(PluginUtil.class.getName());
	}
	
	// prevent cloning
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	private static Map<PluginElement<?>, Identity> identityCache;
	
	private static Map<ExtensionPoint, Collection<Extension>> links;

	public static Identity getIdentity(PluginElement<?> element) {
		if(element==null)
			throw new IllegalArgumentException("Invalid element"); //$NON-NLS-1$
		
		if(identityCache==null) {
			identityCache = Collections.synchronizedMap(
					new HashMap<PluginElement<?>, Identity>());
		}
		
		Identity identity = identityCache.get(element);
		if(identity==null) {
			synchronized (PluginUtil.class) {
				if(!identityCache.containsKey(element)) {
					if(element instanceof Extension) {
						identity = new ExtensionIdentity((Extension) element);
					} else {
						identity = new StaticIdentity(element.getId(), element);
					}
					identityCache.put(element, identity);
				} else {
					identity = identityCache.get(element);
				}
			}
		}
		
		return identity;
	}
	
	private static synchronized void loadLinks() {
		if(links!=null) {
			return;
		}
		
		links = new HashMap<>();
		
		ExtensionPoint extensionPoint = getPluginRegistry().getExtensionPoint(
				CORE_PLUGIN_ID, "Link"); //$NON-NLS-1$
		
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			Extension targetExtension = extension.getParameter("extension").valueAsExtension(); //$NON-NLS-1$
			
			// Prevent weird self relinking
			if(extension==targetExtension) {
				continue;
			}
			
			for(Extension.Parameter param : extension.getParameters("extension-point")) { //$NON-NLS-1$
				ExtensionPoint targetExtensionPoint = param.valueAsExtensionPoint();
				
				// No linking to Link extension-point!
				if(targetExtensionPoint==extensionPoint) {
					continue;
				}
				
				Collection<Extension> list = links.get(targetExtensionPoint);
				if(list==null) {
					list = new LinkedList<>();
					links.put(targetExtensionPoint, list);
				}
				list.add(targetExtension);
			}
		}
	}
	
	public static Collection<Extension> getLinkedExtensions(ExtensionPoint extensionPoint) {
		if(extensionPoint==null)
			throw new IllegalArgumentException("Invalid extension-point"); //$NON-NLS-1$
		
		if(links==null) {
			loadLinks();
		}
		
		Collection<Extension> linkedExtensions = links.get(extensionPoint);
		if(linkedExtensions==null || linkedExtensions.isEmpty()) {
			return Collections.emptyList();
		}
		
		return Collections.unmodifiableCollection(linkedExtensions);
	}
	
	public static Collection<Extension> getExtensions(String extensionPointUid, 
			boolean includeLinked, boolean includeDescendants, Filter filter) {
		
		ExtensionPoint extensionPoint = getPluginRegistry().getExtensionPoint(extensionPointUid);
		
		return getExtensions(extensionPoint, includeLinked, includeDescendants, filter);
	}
	
	public static Collection<Extension> getExtensions(ExtensionPoint extensionPoint, 
			boolean includeLinked, boolean includeDescendants, Filter filter) {
		Collection<Extension> extensions = new HashSet<>();
		
		// Add directly connected extensions
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(filter==null || filter.accepts(extension)) {
				extensions.add(extension);
			}
		}
		
		// Add extensions of descendants
		if(includeDescendants) {
			Collection<ExtensionPoint> descendants = extensionPoint.getDescendants();
			if(descendants!=null && !descendants.isEmpty()) {
				for(ExtensionPoint descendant : descendants) {
					for(Extension extension : descendant.getConnectedExtensions()) {
						if(filter==null || filter.accepts(extension)) {
							extensions.add(extension);
						}
					}
				}
			}
		}
		
		// Add linked extensions
		if(includeLinked) {
			for(Extension extension : getLinkedExtensions(extensionPoint)) {
				if(filter==null || filter.accepts(extension)) {
					extensions.add(extension);
				}
			}
		}
		
		return extensions;
	}
	
	public static Collection<Extension> getExtensions(String[] uniqueIds) {
		Collection<Extension> extensions = new HashSet<>();
		
		for(String uniqueId : uniqueIds) {
			extensions.add(getExtension(uniqueId));
		}
		
		return extensions;
	}
	
	public static void load(Logger logger) throws Exception {
		if(pluginRegistry!=null)
			throw new IllegalStateException("Plug-in registry object already loaded"); //$NON-NLS-1$
		if(pathResolver!=null)
			throw new IllegalStateException("Path resolver object already loaded"); //$NON-NLS-1$
		if(pluginManager!=null)
			throw new IllegalStateException("Plug-in manager object already loaded"); //$NON-NLS-1$
		
		// Init plug-in management objects
		ObjectFactory objectFactory = ObjectFactory.newInstance();
		logger.info("Using object factory: "+objectFactory); //$NON-NLS-1$
		
		pluginRegistry = objectFactory.createRegistry();
		logger.info("Using plugin registry: "+pluginRegistry); //$NON-NLS-1$
		
		//pathResolver = objectFactory.createPathResolver();
		pathResolver = new LibPathResolver();
		logger.info("Using path resolver: "+pathResolver); //$NON-NLS-1$
		
		pluginManager = objectFactory.createManager(pluginRegistry, pathResolver);
		logger.info("Using plugin manager: "+pluginManager); //$NON-NLS-1$
	}
	
	public static void loadAttributes(PluginDescriptor descriptor) {
		if(getPluginManager().isBadPlugin(descriptor)
				|| !getPluginManager().isPluginEnabled(descriptor)) {
			return;
		}
		
		try {
			ClassLoader classLoader = getPluginManager().getPluginClassLoader(descriptor);
			
			// Load resources
			Collection<PluginAttribute> resourceAttributes = descriptor.getAttributes(RESOURCES_KEY); 
			if(resourceAttributes!=null && !resourceAttributes.isEmpty()) {
				for(PluginAttribute attribute : resourceAttributes) {
					ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
					ResourceManager.getInstance().addResource(attribute.getValue(), resourceLoader);
				}
			}
			
			// Load icons
			Collection<PluginAttribute> iconAttributes = descriptor.getAttributes(ICONS_KEY); 
			if(iconAttributes!=null && !iconAttributes.isEmpty()) {
				for(PluginAttribute attribute : iconAttributes) {
					IconRegistry.getGlobalRegistry().addSearchPath(classLoader, attribute.getValue());
				}
			}
			
			// Load preferences
			Collection<PluginAttribute> preferencesAttributes = descriptor.getAttributes(PREFERENCES_KEY); 
			if(preferencesAttributes!=null && !preferencesAttributes.isEmpty()) {
				for(PluginAttribute attribute : preferencesAttributes) {
					classLoader.loadClass(attribute.getValue()).newInstance();
				}
			}
		} catch(Exception e) {
			LoggerFactory.log(PluginUtil.class, Level.SEVERE, 
					"Failed to process plugin attributes: "+descriptor.getUniqueId(), e); //$NON-NLS-1$
		}
	}
	
	public static ClassLoader getClassLoader(PluginElement<?> element) {
		return getPluginManager().getPluginClassLoader(element.getDeclaringPluginDescriptor());
	}
	
	public static Map<String, Object> getProperties(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		Extension.Parameter propertiesParam = null;
		
		try {
			propertiesParam = extension.getParameter("properties"); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			return null;
		}
		
		// call to getParameter(string) should throw IllegalArgumentException if not present!
		assert propertiesParam!=null : "invalid extension implementation!"; //$NON-NLS-1$
		
		Map<String, Object> map = new HashMap<>();
		feedProperties(map, propertiesParam);
		return map;
	}
	
	private static void feedProperties(Map<String, Object> map, Extension.Parameter param) {
		if(param==null) {
			return;
		}
		
		Collection<Extension.Parameter> subParams = param.getSubParameters();
		if(subParams==null || subParams.isEmpty()) {
			map.put(param.getId(), param.valueAsString());
		} else {
			Map<String, Object> subMap = new HashMap<>();
			for(Extension.Parameter subParam : subParams) {
				feedProperties(subMap, subParam);
			}
			map.put(param.getId(), subMap);
		}
	}
	
	public static PluginManager getPluginManager() {
		if(pluginManager==null)
			throw new IllegalStateException("Plug-in manager not yet loaded!"); //$NON-NLS-1$
		
		return pluginManager;
	}

	public static PluginRegistry getPluginRegistry() {
		if(pluginRegistry==null)
			throw new IllegalStateException("Plug-in registry not yet loaded!"); //$NON-NLS-1$
		return pluginRegistry;
	}

	public static PathResolver getPathResolver() {
		if(pathResolver==null)
			throw new IllegalStateException("Path resolver not yet loaded!"); //$NON-NLS-1$
		return pathResolver;
	}
	
	public static PluginDescriptor getCorePlugin() {
		return getPluginRegistry().getPluginDescriptor(CORE_PLUGIN_ID);
	}
	
	public static Extension getExtension(String uid) {
		String pluginId = getPluginRegistry().extractPluginId(uid);
		String elementId = getPluginRegistry().extractId(uid);
		
		if(pluginId==null || elementId==null) {
			return null;
		}
		
		return getPluginRegistry().getPluginDescriptor(pluginId).getExtension(elementId);
	}
	
	public static void activatePlugin(PluginElement<?> element) throws PluginLifecycleException {
		PluginDescriptor descriptor = element.getDeclaringPluginDescriptor();
		if(!getPluginManager().isPluginActivating(descriptor)) {
			getPluginManager().activatePlugin(descriptor.getId());
		}
	}
	
	public static Object instantiate(Extension extension) throws InstantiationException, 
			IllegalAccessException, ClassNotFoundException {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		try {
			activatePlugin(extension);
		} catch (PluginLifecycleException e) {
			LoggerFactory.log(PluginUtil.class, Level.SEVERE, "Failed to activate plug-in: "+extension.getDeclaringPluginDescriptor().getId(), e); //$NON-NLS-1$
			
			throw new IllegalStateException("Plug-in not active for extension: "+extension.getUniqueId());  //$NON-NLS-1$
		}
		
		Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
		if(param==null)
			throw new IllegalArgumentException("Extension does not declare class parameter: "+extension.getUniqueId()); //$NON-NLS-1$
		
		ClassLoader loader = getClassLoader(extension);
		Class<?> clazz = loader.loadClass(param.valueAsString());
		return clazz.newInstance();
	}
	
	public static Class<?> loadClass(Extension extension) throws ClassNotFoundException {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		try {
			activatePlugin(extension);
		} catch (PluginLifecycleException e) {
			LoggerFactory.log(PluginUtil.class, Level.SEVERE, "Failed to activate plug-in: "+extension.getDeclaringPluginDescriptor().getId(), e); //$NON-NLS-1$
			
			throw new IllegalStateException("Plug-in not active for extension: "+extension.getUniqueId());  //$NON-NLS-1$
		}
		
		Extension.Parameter param = extension.getParameter("class"); //$NON-NLS-1$
		if(param==null)
			throw new IllegalArgumentException("Extension does not declare class parameter: "+extension.getUniqueId()); //$NON-NLS-1$
		
		ClassLoader loader = getClassLoader(extension);
		return loader.loadClass(param.valueAsString());
	}
	
	public static Class<?> loadClass(Extension.Parameter param) throws ClassNotFoundException {
		if(param==null)
			throw new IllegalArgumentException("Invalid parameter"); //$NON-NLS-1$
		
		Extension extension = param.getDeclaringExtension();
		
		try {
			activatePlugin(extension);
		} catch (PluginLifecycleException e) {
			LoggerFactory.log(PluginUtil.class, Level.SEVERE, "Failed to activate plug-in: "+extension.getDeclaringPluginDescriptor().getId(), e); //$NON-NLS-1$
			
			throw new IllegalStateException("Plug-in not active for extension: "+extension.getUniqueId());  //$NON-NLS-1$
		}
		
		ClassLoader loader = getClassLoader(extension);
		return loader.loadClass(param.valueAsString());
	}
	
	public static boolean isExtensionOf(Extension extension, ExtensionPoint extensionPoint) {
		ExtensionPoint target = getPluginRegistry().getPluginDescriptor(
				extension.getExtendedPluginId()).getExtensionPoint(
						extension.getExtendedPointId());
		return  target==extensionPoint || target.isSuccessorOf(extensionPoint);
	}

	public static final Comparator<org.java.plugin.registry.Identity> IDENTITY_COMPARATOR = new Comparator<org.java.plugin.registry.Identity>() {
	
		@Override
		public int compare(org.java.plugin.registry.Identity o1,
				org.java.plugin.registry.Identity o2) {
			return o1.getId().compareTo(o2.getId());
		}
	
	};
	
	/**
	 * Sorts extensions by their identity in case they extend the
	 * {@code Localizable} extension-point or by their unique id
	 * otherwise.
	 */
	public static final Comparator<Extension> EXTENSION_COMPARATOR = new Comparator<Extension>() {

		@Override
		public int compare(Extension e1, Extension e2) {
			if(e1==e2) {
				return 0;
			}
			
			Identity id1 = getIdentity(e1);
			Identity id2 = getIdentity(e2);
			
			if(id1!=null && id2!=null) {
				return Identity.COMPARATOR.compare(id1, id2);
			} else {
				return e1.getUniqueId().compareTo(e2.getUniqueId());
			}
		}
		
	};

	public static Extension findExtension(String pluginId, String extensionPointId, String uid) {
		PluginDescriptor descriptor = getPluginManager().getRegistry().getPluginDescriptor(pluginId);
		if(descriptor==null) {
			return null;
		}
		ExtensionPoint extensionPoint = descriptor.getExtensionPoint(extensionPointId);
		if(extensionPoint==null) {
			return null;
		}
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(extension.getUniqueId().equals(uid)) {
				return extension;
			}
		}
		return null;
	}
	
	public static Collection<Extension> findExtensions(ExtensionPoint extensionPoint, Filter filter) {
		Collection<Extension> extensions = new LinkedHashSet<>();
		
		for(Extension extension : extensionPoint.getConnectedExtensions()) {
			if(filter==null || filter.accepts(extension)) {
				extensions.add(extension);
			}
		}
		
		Collection<ExtensionPoint> descendants = extensionPoint.getDescendants();
		if(descendants!=null && !descendants.isEmpty()) {
			for(ExtensionPoint descendant : descendants) {
				for(Extension extension : descendant.getConnectedExtensions()) {
					if(filter==null || filter.accepts(extension)) {
						extensions.add(extension);
					}
				}
			}
		}
		
		return extensions;
	}
	
	public static Filter createCapabilityFilter(final Capability capability, final boolean generalize) {
		return new Filter() {
			
			@Override
			public boolean accepts(Object obj) {
				Extension extension = (Extension)obj;
				return hasCapability(extension, capability, generalize);
			}
		};
	}
	
	public static Extension showExtensionDialog(Component parent, 
			String title, ExtensionPoint extensionPoint, boolean deepSearch, boolean doSort) {
		Collection<Extension> extensions = null;
		if(deepSearch) {
			extensions = findExtensions(extensionPoint, null);
		} else {
			extensions = extensionPoint.getConnectedExtensions();
		}
		
		return showExtensionDialog(parent, title, extensions, doSort);
	}
	
	public static Extension showExtensionDialog(Component parent, 
			String title, Collection<Extension> extensions, boolean doSort) {
		
		// Nothing more to do in case there are no extensions given
		if(extensions==null ||extensions.isEmpty()) {
			return null;
		}
		
		ExtensionListModel model = new ExtensionListModel(extensions, doSort);
		final JList<Extension> list = new JList<>(model);
		list.setCellRenderer(ExtensionListCellRenderer.getSharedInstance());
		
		final MutableBoolean selectedByClick = new MutableBoolean(false);
		
		list.addMouseListener(new MouseAdapter() {

			/**
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()!=2) {
					return;
				}
				
				JDialog dialog = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, list);
				if(dialog==null) {
					return;
				}
				
				selectedByClick.setValue(true);
				dialog.setVisible(false);
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(250, 200));
		
		if(!DialogFactory.getGlobalFactory().showGenericDialog(
				parent, title, null, scrollPane, false, "ok", "cancel")  //$NON-NLS-1$ //$NON-NLS-2$
				&& !selectedByClick.getValue()) {
			return null;
		}
		
		return list.getSelectedValue();
	}
	
	private static Collection<Parameter> extractCapabilities(Extension extension) {
		Collection<Parameter> parameters = null;
		
		try {
			Parameter param = extension.getParameter("capabilities"); //$NON-NLS-1$
			if(param!=null) {
				parameters = param.getSubParameters();
			}
		} catch(IllegalArgumentException e) {
			// ignore
		}
		
		if(parameters==null) {
			parameters = Collections.emptyList();
		}
		
		return parameters;
	}
	
	public static List<Capability> getCapabilities(Extension extension) {
		if(extension==null)
			throw new IllegalArgumentException("Invalid extension"); //$NON-NLS-1$
		
		List<Capability> capabilities = new ArrayList<>();
		Collection<Parameter> params = extractCapabilities(extension);
		
		// Convert declarations into actual capability objects
		for(Extension.Parameter param : params) {
			String command = param.getId();
			ContentType contentType = ContentTypeRegistry.getInstance().getType(param.valueAsString());
			capabilities.add(Capability.getCapability(command, contentType));
		}
		
		return capabilities;
	}
	
	public static boolean hasCapability(Extension extension, Capability capability, boolean generalize) {

		for(Capability cap : getCapabilities(extension)) {
			if((generalize && capability.isGeneralizationOf(cap))
					|| (!generalize && capability.equals(cap))) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasCapability(Extension extension, Capability...capabilities) {
		if(capabilities==null || capabilities.length==0)
			throw new IllegalArgumentException("Invalid or empty capabilities list"); //$NON-NLS-1$

		Set<Capability> caps = new HashSet<>(getCapabilities(extension));
		if(caps.isEmpty()) {
			return false;
		}
		
		for(Capability capability : capabilities) {
			if(caps.contains(capability)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean hasAllCapability(Extension extension, Capability...capabilities) {
		if(capabilities==null || capabilities.length==0)
			throw new IllegalArgumentException("Invalid or empty capabilities list"); //$NON-NLS-1$

		Set<Capability> caps = new HashSet<>(getCapabilities(extension));
		if(caps.isEmpty()) {
			return false;
		}
		
		for(Capability capability : capabilities) {
			if(!caps.contains(capability)) {
				return false;
			}
		}
		
		return true;
	}
}
