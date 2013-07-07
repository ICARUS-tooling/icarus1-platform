/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.search_tools.view;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.ManagementConstants;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.ui.events.EventObject;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchPerspective extends Perspective {

	public SearchPerspective() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);
	}

	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();
		
		String[] defaultViewIds = {
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
				// TODO
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));
		
		// Collect all extensions that are connected to the SearchToolsView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("SearchToolsView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(managementViewPoint, 
					true, true, null));
		}
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

	/*@Override
	protected Alignment getViewAlignment(Extension extension) {
		if(ManagementConstants.DEFAULT_LOG_VIEW_ID.equals(extension.getUniqueId())) {
			return Alignment.LEFT;
		} else {
			return super.getViewAlignment(extension);
		}
	}*/	
}
