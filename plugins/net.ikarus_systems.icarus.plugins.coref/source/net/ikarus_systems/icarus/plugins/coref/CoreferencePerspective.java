/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.coref;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import net.ikarus_systems.icarus.language.coref.CoreferenceDocumentData;
import net.ikarus_systems.icarus.language.coref.CoreferenceDocumentSet;
import net.ikarus_systems.icarus.plugins.core.Perspective;
import net.ikarus_systems.icarus.plugins.coref.io.CONLL12DocumentReader;
import net.ikarus_systems.icarus.plugins.coref.view.CoreferenceDocumentSetPresenter;
import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.Locations;

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
	 * @see net.ikarus_systems.icarus.plugins.core.Perspective#init(javax.swing.JComponent)
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
