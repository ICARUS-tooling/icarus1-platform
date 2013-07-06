/*
 * $Revision: 41 $
 * $Date: 2013-05-21 00:46:47 +0200 (Di, 21 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.language_tools/source/net/ikarus_systems/icarus/plugins/language_tools/treebank/TreebankManagerPerspective.java $
 *
 * $LastChangedDate: 2013-05-21 00:46:47 +0200 (Di, 21 Mai 2013) $ 
 * $LastChangedRevision: 41 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.language_tools.treebank;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;


import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.language_tools.LanguageToolsConstants;
import de.ims.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id: TreebankManagerPerspective.java 41 2013-05-20 22:46:47Z mcgaerty $
 *
 */
public class TreebankManagerPerspective extends Perspective {
	
	public static final String PERSPECTIVE_ID = LanguageToolsConstants.TREEBANK_MANAGER_PERSPECTIVE_ID;

	
	public TreebankManagerPerspective() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);
		
		focusView(LanguageToolsConstants.TREEBANK_EXPLORER_VIEW_ID);
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
				ManagementConstants.TABLE_VIEW_ID,
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
	 * @see de.ims.icarus.plugins.core.Perspective#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}
}
