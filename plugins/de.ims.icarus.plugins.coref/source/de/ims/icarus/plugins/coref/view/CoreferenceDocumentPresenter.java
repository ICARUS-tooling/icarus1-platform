/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view;

import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceUtils;
import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.util.data.ContentType;

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
		
		doc.appendBatchCoreferenceDocumentData(data);
		
		doc.applyBatchUpdates(0);
		
		return true;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#isPresenting()
	 */
	@Override
	public boolean isPresenting() {
		return data!=null;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#getPresentedData()
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
