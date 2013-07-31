/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 *
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
