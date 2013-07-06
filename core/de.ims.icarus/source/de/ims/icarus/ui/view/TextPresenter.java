/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.view;

import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextPresenter extends AbstractEditorPanePresenter<String> {

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible("StringContentType", type); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		
		if(data.equals(presentedData)) {
			return;
		}
		
		presentedData = (String) data;
		
		if(contentPane!=null) {
			refresh();
		}
	}

	/**
	 * @see de.ims.icarus.ui.view.AbstractEditorPanePresenter#getDefaultText()
	 */
	@Override
	protected String getDefaultText() {
		return null;
	}

	/**
	 * @see de.ims.icarus.ui.view.AbstractEditorPanePresenter#refresh()
	 */
	@Override
	protected void refresh() {
		contentPane.setText(presentedData);
	}

}
