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
package de.ims.icarus.util.annotation;

import de.ims.icarus.util.data.ContentType;

/**
 * Marks an object that contains other annotated objects
 * and is aware of the annotation type used. This interface
 * plays an important role for determining the actual
 * implementation of an {@code AnnotationManager} to be used for
 * managing the annotated data. Therefore it serves as a bridging
 * point for framework tools that rely on valid information about
 * the type of annotation used for data they display but of which
 * they are not aware.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationContainer {

	/**
	 * Returns the {@code ContentType} that describes the annotations
	 * used for data within this container.
	 */
	ContentType getAnnotationType();
}
