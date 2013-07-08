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

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.core.Perspective;
import de.ims.icarus.plugins.coref.io.CONLL12DocumentReader;
import de.ims.icarus.plugins.coref.view.CoreferenceDocumentSetPresenter;
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
			//test(container);
			textGraph(container);
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
		reader.init(location, null);
		CoreferenceDocumentSet set = new CoreferenceDocumentSet();
		CoreferenceDocumentData documentData;
		while((documentData=reader.next())!=null) {
			set.add(documentData);
			/*if(set.size()>20) {
				break;
			}*/
		}
		
		CoreferenceDocumentSetPresenter presenter = new CoreferenceDocumentSetPresenter();
		
		presenter.present(set, null);
		
		container.setLayout(new BorderLayout());
		container.add(presenter.getPresentingComponent(), BorderLayout.CENTER);
	}
}
