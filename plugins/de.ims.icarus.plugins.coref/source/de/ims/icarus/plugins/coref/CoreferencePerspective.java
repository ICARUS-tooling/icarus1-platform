/*
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
import java.util.logging.Level;

import javax.swing.JComponent;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.io.AllocationReader;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.coref.io.CONLL12DocumentReader;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentSetPresenter;
import de.ims.icarus.plugins.coref.view.graph.CoreferenceGraphPresenter;
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
		try {
			test2(container);
			//textGraph(container);
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, "Failed to test", e);
		}
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
		
		CoreferenceDocumentSet set = CoreferenceDocumentSet.loadDocumentSet(
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
		
		CoreferenceDocumentSet set = CoreferenceDocumentSet.loadDocumentSet(
				reader, location, new Options());
		
		String path = "E:\\Tasks\\Diplomarbeit\\resources\\out.GOLD.icarus"; //$NON-NLS-1$
		AllocationReader r = new AllocationReader();
		r.init(Locations.getFileLocation(path), null, set);		
		CoreferenceAllocation gold = r.readAllocation();
		
		path = "E:\\Tasks\\Diplomarbeit\\resources\\out.PRED.icarus"; //$NON-NLS-1$
		r = new AllocationReader();
		r.init(Locations.getFileLocation(path), null, set);		
		CoreferenceAllocation predicted = r.readAllocation();		
		
		CoreferenceGraphPresenter presenter = new CoreferenceGraphPresenter();
		
		container.setLayout(new BorderLayout());
		container.add(presenter.getPresentingComponent(), BorderLayout.CENTER);

		CoreferenceDocumentData document = set.get(0);
		Options options = new Options();
		options.put("edges", predicted.getEdgeSet(document.getId()));
		options.put("goldEdges", gold.getEdgeSet(document.getId()));
		presenter.present(document, options);
		
	}
}
