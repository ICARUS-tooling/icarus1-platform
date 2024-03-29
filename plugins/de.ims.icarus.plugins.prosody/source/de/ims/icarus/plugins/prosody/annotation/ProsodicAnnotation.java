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
package de.ims.icarus.plugins.prosody.annotation;

import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.annotation.SearchAnnotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ProsodicAnnotation extends SearchAnnotation {


	boolean isHighlighted(int index, int sylIndex);

	int getGroupId(int index, int sylIndex);

	int getGroupId(int index, int sylIndex, String token);

	boolean isNodeHighlighted(int index, int sylIndex);
	boolean isEdgeHighlighted(int index, int sylIndex);
	boolean isTransitiveHighlighted(int index, int sylIndex);

	long getHighlight(int index, int sylIndex);

	boolean isTokenHighlighted(int index, int sylIndex, String token);

	@Override
	ProsodyResultAnnotator getAnnotator();

	<S extends Object> SearchConstraint[] getConstraints( int index, Class<S> constraintClass);
}
