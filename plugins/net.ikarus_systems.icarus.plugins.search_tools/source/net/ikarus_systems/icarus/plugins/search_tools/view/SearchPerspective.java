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

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.plugins.PluginUtil;
import net.ikarus_systems.icarus.plugins.core.ManagementConstants;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.plugins.search_tools.view.graph.ConstraintGraphPresenter;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.SearchGraph;
import net.ikarus_systems.icarus.search_tools.SearchNode;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;

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
		//collectViewExtensions();
		//defaultDoLayout(container);
		
		SearchGraph dummy = new SearchGraph() {
			
			@Override
			public SearchNode[] getRootNodes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public SearchNode[] getNodes() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public SearchEdge[] getEdges() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		ConstraintGraphPresenter presenter = new ConstraintGraphPresenter();
		presenter.init();
		presenter.setConstraintTargetType(DependencyUtils.getDependencyContentType());
		try {
			presenter.present(dummy, null);
		} catch (UnsupportedPresentationDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		container.setLayout(new BorderLayout());
		container.add(presenter.getPresentingComponent(), BorderLayout.CENTER);
	}

	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();
		
		String[] defaultViewIds = {
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
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
}
