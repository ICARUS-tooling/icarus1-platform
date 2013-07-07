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

import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeCollection;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;

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
