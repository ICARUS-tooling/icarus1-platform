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
package de.ims.icarus.plugins.coref;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.io.AllocationReader;
import de.ims.icarus.language.coref.io.DefaultAllocationReader;
import de.ims.icarus.plugins.PluginUtil;
import de.ims.icarus.plugins.core.ManagementConstants;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.coref.io.CONLL12DocumentReader;
import de.ims.icarus.plugins.coref.view.graph.CoreferenceGraphPresenter;
import de.ims.icarus.plugins.coref.view.grid.EntityGridPresenter;
import de.ims.icarus.plugins.coref.view.text.CoreferenceDocumentPresenter;
import de.ims.icarus.plugins.coref.view.text.CoreferenceDocumentSetPresenter;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.location.Location;
import de.ims.icarus.util.location.Locations;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferencePerspective extends Perspective {

	public CoreferencePerspective() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		collectViewExtensions();
		defaultDoLayout(container);
		
		focusView(CorefConstants.COREFERENCE_MANAGER_VIEW_ID);
		/*try {
			test2(container);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Override
	protected void collectViewExtensions() {
		PluginDescriptor descriptor = getExtension().getDeclaringPluginDescriptor();
		
		String[] defaultViewIds = {
				CorefConstants.COREFERENCE_MANAGER_VIEW_ID,
				CorefConstants.COREFERENCE_EXPLORER_VIEW_ID,
				CorefConstants.COREFERENCE_DOCUMENT_VIEW_ID,
				ManagementConstants.DEFAULT_LOG_VIEW_ID,
				ManagementConstants.DEFAULT_OUTPUT_VIEW_ID,
				/*ManagementConstants.TABLE_VIEW_ID,*/
		};
		
		Set<Extension> newExtensions = new HashSet<>();
		
		// Collect default extensions and report corrupted state
		// when one is missing
		newExtensions.addAll(PluginUtil.getExtensions(defaultViewIds));
		
		// Collect all extensions that are connected to the CoreferenceView point
		// -> might result in redundant adds, so we use a Set<Extension>
		ExtensionPoint managementViewPoint = descriptor.getExtensionPoint("CoreferenceView"); //$NON-NLS-1$
		if(managementViewPoint!=null) {
			newExtensions.addAll(PluginUtil.getExtensions(
					managementViewPoint, true, true, null));
		}
		
		connectedViews.addAll(newExtensions);
		
		eventSource.fireEvent(new EventObject(PerspectiveEvents.VIEWS_ADDED, 
				"extensions", newExtensions.toArray())); //$NON-NLS-1$
	}

	private void textGraph(JComponent container) {
		container.setLayout(new BorderLayout());
		
		mxGraph graph = new mxGraph();
		mxGraphComponent comp = new mxGraphComponent(graph);
		comp.getGraphControl().setBackground(Color.white);
		
		graph.insertVertex(null, null, "test1", 30, 30, 100, 100);
		
		container.add(comp, BorderLayout.CENTER);
		
	}

	private void test(JComponent container) throws Exception {

		Location location = Locations.getFileLocation("data/coref/eng_dev_v4_auto_conll.gz");
		CONLL12DocumentReader reader = new CONLL12DocumentReader();
		
		CoreferenceDocumentSet set = CoreferenceUtils.loadDocumentSet(
				reader, location, new Options());
		
		CoreferenceDocumentSetPresenter presenter = new CoreferenceDocumentSetPresenter();
		
		presenter.present(set, null);
		
		container.setLayout(new BorderLayout());
		container.add(presenter.getPresentingComponent(), BorderLayout.CENTER);
	}

	private void test2(JComponent container) throws Exception {

		Location location = Locations.getFileLocation(
				"data/coref/eng_dev_v4_auto_conll.gz");
		CONLL12DocumentReader reader = new CONLL12DocumentReader();
		
		CoreferenceDocumentSet set = CoreferenceUtils.loadDocumentSet(
				reader, location, new Options());
		
		String path = "E:\\Tasks\\Diplomarbeit\\resources\\out.GOLD.icarus"; //$NON-NLS-1$
		AllocationReader r = new DefaultAllocationReader();
		r.init(Locations.getFileLocation(path), null, set);		
		CoreferenceAllocation gold = new CoreferenceAllocation();
		r.readAllocation(gold);
		
		path = "E:\\Tasks\\Diplomarbeit\\resources\\out.PRED.icarus"; //$NON-NLS-1$
		r = new DefaultAllocationReader();
		r.init(Locations.getFileLocation(path), null, set);		
		CoreferenceAllocation predicted = new CoreferenceAllocation();
		r.readAllocation(predicted);
		
		//CoreferenceGraphPresenter presenter = new CoreferenceGraphPresenter();
		CoreferenceGraphPresenter presenter = new CoreferenceGraphPresenter();
		
		container.setLayout(new BorderLayout());
		container.add(presenter.getPresentingComponent(), BorderLayout.CENTER);

		CoreferenceDocumentData document = set.get(0);
		Options options = new Options();
		options.put("allocation", predicted);
		options.put("goldAllocation", gold);
		presenter.present(document, options);
		
		//container.add(new JLabel("footer"), BorderLayout.SOUTH);
	}
}
