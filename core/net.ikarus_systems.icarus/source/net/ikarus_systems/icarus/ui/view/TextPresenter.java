/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.view;

import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TextPresenter extends AbstractEditorPanePresenter<String> {

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible("StringContentType", type); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.Presenter#present(java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null) {
			return;
		}		
		if(data==presentedData) {
			return;
		}
		
		presentedData = (String) data;
		
		if(contentPane!=null) {
			refresh();
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.AbstractEditorPanePresenter#getDefaultText()
	 */
	@Override
	protected String getDefaultText() {
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.ui.view.AbstractEditorPanePresenter#refresh()
	 */
	@Override
	protected void refresh() {
		contentPane.setText(presentedData);
	}

}
