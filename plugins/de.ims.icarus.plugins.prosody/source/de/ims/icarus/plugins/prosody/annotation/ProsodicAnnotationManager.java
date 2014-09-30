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
 * $Revision: 135 $
 * $Date: 2013-09-04 07:35:22 +0200 (Mi, 04 Sep 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.dependency/source/de/ims/icarus/language/dependency/annotation/DependencyAnnotationManager.java $
 *
 * $LastChangedDate: 2013-09-04 07:35:22 +0200 (Mi, 04 Sep 2013) $
 * $LastChangedRevision: 135 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.prosody.annotation;

import de.ims.icarus.language.dependency.annotation.DependencyAnnotationManager;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id: DependencyAnnotationManager.java 135 2013-09-04 05:35:22Z mcgaerty $
 *
 */
public class ProsodicAnnotationManager extends DependencyAnnotationManager {

	public ProsodicAnnotationManager() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationManager#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return ProsodyUtils.getProsodyAnnotationType();
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractSearchAnnotationManager#createCompositeHighlight(long[])
	 */
	@Override
	protected long createCompositeHighlight(long[] highlights) {
		return ProsodyHighlighting.getInstance().createCompositeHighlight(highlights);
	}
}
