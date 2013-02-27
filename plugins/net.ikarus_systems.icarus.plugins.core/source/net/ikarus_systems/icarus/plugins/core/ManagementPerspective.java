/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.resources.DefaultResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceLoader;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CorruptedStateException;

import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class ManagementPerspective extends Perspective {
	
	public static final String PERSPECTIVE_ID = ManagementConstants.MANAGEMENT_PERSPECTIVE_ID;
	
	static {
		try {
			PluginManager pluginManager = PluginUtil.getPluginManager();
			PluginDescriptor descriptor = pluginManager.getRegistry().getPluginDescriptor(IcarusCorePlugin.PLUGIN_ID);
			// Make our resources accessible via the global domain
			ResourceLoader resourceLoader = new DefaultResourceLoader(pluginManager.getPluginClassLoader(descriptor));
			ResourceManager.getInstance().addResource(
					"net.ikarus_systems.icarus.plugins.core.resources.management", resourceLoader); //$NON-NLS-1$
			
		} catch(Exception e) {
			LoggerFactory.getLogger(ManagementPerspective.class).log(LoggerFactory.record(
					Level.SEVERE, "Unable to add resources", e)); //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	public ManagementPerspective() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);
		
		UIUtil.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				View viewToFocus = getView(ManagementConstants.PLUGIN_EXPLORER_VIEW_ID);
				if(viewToFocus!=null) {
					viewToFocus.focusView();
				}
			}
		});
	}
	
	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();
		
		String[] defaultViewIds = {
				ManagementConstants.PLUGIN_EXPLORER_VIEW_ID,
				ManagementConstants.EXTENSION_POINT_OUTLINE_VIEW_ID,
				ManagementConstants.EXTENSION_POINT_HIERARCHY_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		for(String viewId : defaultViewIds) {
			Extension extension = descriptor.getExtension(viewId);
			if(extension==null)
				throw new CorruptedStateException("Missing default extension: "+viewId); //$NON-NLS-1$
			
			newExtensions.add(extension);
		}
		
		// Collect all extensions that are connected to the ManagementView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("ManagementView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(managementViewPoint.getConnectedExtensions());
		}
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}
}
