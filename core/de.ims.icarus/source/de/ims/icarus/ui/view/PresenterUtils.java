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

import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeCollection;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class PresenterUtils {

	private PresenterUtils() {
		// no-op
	}
	
	public static boolean presenterSupports(Presenter presenter, Object data) {
		if(data==null) {
			return false;
		}
		
		ContentTypeCollection collection = ContentTypeRegistry.getInstance().getEnclosingTypes(data);
		if(collection==null || collection.isEmpty()) {
			return false;
		}
		
		return presenterSupports(presenter, collection);
	}

	public static boolean presenterSupports(Presenter presenter, ContentTypeCollection collection) {
		for(ContentType type : collection.getContentTypes()) {
			if(presenter.supports(type)) {
				return true;
			}
		}
		return false;
	}
}
