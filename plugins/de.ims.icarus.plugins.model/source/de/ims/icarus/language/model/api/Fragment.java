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
package de.ims.icarus.language.model.api;

import de.ims.icarus.language.model.api.layer.FragmentLayer;
import de.ims.icarus.language.model.api.raster.Position;
import de.ims.icarus.language.model.api.raster.PositionOutOfBoundsException;

/**
 * A {@code Fragment} allows for the definition of {@code Markable} objects that
 * are not bound by the logical structure of a corpus's base layer. A regular
 * markable references parts of other existing markables, like pre-tokenized
 * and/or split objects in a text that forms
 * the actual corpus. With the use of fragments it is possible to handle <i>raw</i>
 * text data. Note however, that using fragments is much more expensive than using
 * markables, since a lookup structure has to be built for each markable object that
 * contains fragments, in order to visualize or explore it.
 * <p>
 * As a precondition for the use of fragments, a <i>form</i> annotation-layer has to be
 * present. The indices returned by the boundary methods ({@link #getFragmentBeginIndex()}
 * and {@link #getFragmentEndIndex()}) are references to the surface forms of the
 * respective markables. All those indices are character based.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 *@see Markable
 */
public interface Fragment extends Markable {

	@Override
	FragmentLayer getLayer();

	/**
	 * Returns the markable this fragment is a part of.
	 *
	 * @return
	 */
	Markable getMarkable();

	/**
	 * Returns the position within the surrounding markable of
	 * this fragment that denotes the actual begin of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentBegin();

	/**
	 * Returns the position within the surrounding markable of
	 * this fragment that denotes the actual end of the fragment itself.
	 *
	 * @return
	 */
	Position getFragmentEnd();

	// Modification methods

	/**
	 * Changes the begin position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @throws PositionOutOfBoundsException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting markable
	 */
	void setFragmentBegin(Position position);

	/**
	 * Changes the end position of the fragment to the new {@code position}.
	 *
	 * @param position
	 * @throws PositionOutOfBoundsException if the {@code position} violates
	 * the bounds specified by the raster size of the hosting markable
	 */
	void setFragmentEnd(Position position);
}
