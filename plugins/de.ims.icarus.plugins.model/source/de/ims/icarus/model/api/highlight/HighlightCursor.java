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
package de.ims.icarus.model.api.highlight;

import de.ims.icarus.model.api.layer.HighlightLayer;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 * @see Highlight
 *
 */
public interface HighlightCursor {

	/**
	 * Returns the {@code HighlightLayer} this cursor originated from
	 */
	HighlightLayer getHighlightLayer();

	/**
	 * Returns the number of concurrently available highlight information
	 * accessible through this cursor.
	 */
	int highlightCount();

	/**
	 * Returns the position of the given {@code Highlight} object within this cursor.
	 *
	 * @throws NullPointerException if the {@code highlight} argument is {@code null}
	 * @throws IllegalArgumentException if the given {@code Highlight} is not a member of
	 * this cursor
	 */
	int indexOf(Highlight highlight);

	/**
	 * Returns the {@code Highlight} stored at the given index.
	 *
	 * @throws IndexOutOfBoundsException if the index is out of range
	 *         (<tt>index &lt; 0 || index &gt;= highlightCount()</tt>)
	 */
	Highlight getHighlight(int index);
}
