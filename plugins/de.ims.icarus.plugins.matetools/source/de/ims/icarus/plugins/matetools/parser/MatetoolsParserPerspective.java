/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.matetools.parser;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;


import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.jgraph.JGraphConstants;
import de.ims.icarus.plugins.matetools.MatetoolsConstants;
import de.ims.icarus.ui.events.EventObject;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MatetoolsParserPerspective extends Perspective {
	
	public static final String PERSPECTIVE_ID = MatetoolsConstants.MATETOOLS_PARSER_PERSPECTIVE_ID;

	public MatetoolsParserPerspective() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);

		
		focusView(MatetoolsConstants.MATETOOLS_PARSER_INPUT_VIEW_ID);
	}
	
	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();
		
		String[] defaultViewIds = {
				MatetoolsConstants.MATETOOLS_PARSER_INPUT_VIEW_ID,
				JGraphConstants.LIST_GRAPH_VIEW_ID,
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
				ManagementConstants.TABLE_VIEW_ID,
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));

		// Collect all extensions that are connected to the MatetoolsView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint matetoolsViewPoint = descriptor.getExtensionPoint("MatetoolsView"); //$NON-NLS-1$
		if(matetoolsViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					matetoolsViewPoint, true, true, null));
		}
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}
}