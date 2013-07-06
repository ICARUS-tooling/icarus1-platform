/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.coref.view;

import net.ikarus_systems.icarus.language.coref.CoreferenceDocumentData;
import net.ikarus_systems.icarus.language.coref.CoreferenceUtils;
import net.ikarus_systems.icarus.language.coref.text.CoreferenceDocument;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CoreferenceDocumentPresenter extends AbstractCoreferenceTextPresenter {
	
	protected CoreferenceDocumentData data;

	public CoreferenceDocumentPresenter() {
		// no-op
	}

	@Override
	public ContentType getContentType() {
		return CoreferenceUtils.getCoreferenceDocumentContentType();
	}

	@Override
	protected void setData(Object data) {
		this.data = (CoreferenceDocumentData) data;
	}

	@Override
	protected boolean buildDocument(CoreferenceDocument doc) throws Exception {		
		if(data==null) {
			return false;
		}
		
		int size = data.size();
		for(int i=0; i<size; i++) {
			if(Thread.currentThread().isInterrupted()) {
				return false;
			}
			doc.appendBatchCoreferenceData(data.get(i));
		}
		
		doc.applyBatchUpdates(0);
		
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#getPresentedData()
	 */
	@Override
	public CoreferenceDocumentData getPresentedData() {
		return data;
	}

	@Override
	protected CoreferenceDocument getDocument() {
		// Make sure our components are created
		if(textPane==null) {
			getPresentingComponent();
		}
		
		return (CoreferenceDocument) textPane.getDocument();
	}
}
