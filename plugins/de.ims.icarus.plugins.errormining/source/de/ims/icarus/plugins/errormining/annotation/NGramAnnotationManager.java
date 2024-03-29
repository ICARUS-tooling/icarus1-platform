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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.errormining.annotation;

import de.ims.icarus.search_tools.annotation.AbstractSearchAnnotationManager;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Gregor Thiele
 * @version $Id$
 *
 */
public class NGramAnnotationManager extends AbstractSearchAnnotationManager {
	
	public NGramAnnotationManager(){
		//noop
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationManager#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		// TODO passt dependency anno type?
		//return DependencyUtils.getDependencyAnnotationType();
		return ContentTypeRegistry.getInstance().getTypeForClass(NGramAnnotation.class);
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractSearchAnnotationManager#createCompositeHighlight(long[])
	 */
	@Override
	protected long createCompositeHighlight(long[] highlights) {
		return NGramHighlighting.getInstance().createCompositeHighlight(highlights);
	}

}
