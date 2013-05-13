/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.language_tools.treebank;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.ManagementConstants;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.plugins.language_tools.LanguageToolsConstants;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.events.EventObject;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TreebankManagerPerspective extends Perspective {
	
	public static final String PERSPECTIVE_ID = LanguageToolsConstants.TREEBANK_MANAGER_PERSPECTIVE_ID;

	
	public TreebankManagerPerspective() {
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
				View viewToFocus = getView(LanguageToolsConstants.TREEBANK_EXPLORER_VIEW_ID);
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
				LanguageToolsConstants.TREEBANK_EXPLORER_VIEW_ID,
				LanguageToolsConstants.TREEBANK_EDIT_VIEW_ID,
				LanguageToolsConstants.TREEBANK_PROPERTIES_VIEW_ID,
				/*LanguageToolsConstants.TREEBANK_INSPECT_VIEW_ID,*/
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));
		
		// Collect all extensions that are connected to the TreebankManagementView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("TreebankManagementView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					managementViewPoint, true, true, null));
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
