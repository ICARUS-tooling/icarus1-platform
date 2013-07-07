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

import javax.swing.JComponent;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
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
			test(container);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
