package de.ims.icarus.plugins.weblicht;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;


import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.ui.events.EventObject;

public class WeblichtPerspective extends Perspective {

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
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));
		
		// Collect all extensions that are connected to the TreebankManagementView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("WeblichtManagementView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					managementViewPoint, true, true, null));
		}
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

}
