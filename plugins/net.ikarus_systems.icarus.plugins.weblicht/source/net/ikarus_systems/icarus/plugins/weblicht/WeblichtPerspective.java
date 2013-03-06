package net.ikarus_systems.icarus.plugins.weblicht;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CorruptedStateException;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.PluginDescriptor;

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
				WeblichtConstants.WEBLICHT_CHAIN_VIEW_ID,
				WeblichtConstants.WEBLICHT_EDIT_VIEW_ID,
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
		
		// Add some general views
		PluginDescriptor corePlugin = PluginUtil.getCorePlugin();
		newExtensions.add(corePlugin.getExtensionPoint("View").getConnectedExtension( //$NON-NLS-1$
				PluginUtil.CORE_PLUGIN_ID+"@DefaultLogView")); //$NON-NLS-1$
		newExtensions.add(corePlugin.getExtensionPoint("View").getConnectedExtension( //$NON-NLS-1$
				PluginUtil.CORE_PLUGIN_ID+"@DefaultOutputView")); //$NON-NLS-1$
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

}
