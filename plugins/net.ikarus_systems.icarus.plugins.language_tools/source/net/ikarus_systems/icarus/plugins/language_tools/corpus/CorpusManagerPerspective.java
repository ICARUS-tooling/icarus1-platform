/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.corpus;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.util.CorruptedStateException;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusManagerPerspective extends Perspective {
	
	public static final String PERSPECTIVE_ID = LanguageToolsConstants.CORPUS_MANAGER_PERSPECTIVE_ID;

	
	public CorpusManagerPerspective() {
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
				View viewToFocus = getView(LanguageToolsConstants.CORPUS_EXPLORER_VIEW_ID);
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
				LanguageToolsConstants.CORPUS_EXPLORER_VIEW_ID,
				LanguageToolsConstants.CORPUS_EDIT_VIEW_ID,
				LanguageToolsConstants.CORPUS_PROPERTIES_VIEW_ID,
				/*LanguageToolsConstants.CORPUS_INSPECT_VIEW_ID,*/
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
		
		// Collect all extensions that are connected to the CorpusManagementView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("CorpusManagementView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(managementViewPoint.getConnectedExtensions());
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

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}
}
