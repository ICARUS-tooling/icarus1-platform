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

/**
 * A {@code Fragment} allows for the definition of {@code Markable} objects that
 * are not bound by the logical structure of a corpus's base layer. A regular
 * markable references pre-tokenized and/or split objects in a text that forms
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

	/**
	 * Returns the markable this fragment is a part of.
	 * Note that fragments cannot be nested and that only markables
	 * residing in the <i>base layer</i> of a corpus can be fragmented!
	 *
	 * @return
	 */
	Markable getMarkable();

	/**
	 * Returns the character-based index within the surrounding markable of
	 * this fragment that denotes the actual begin of the fragment itself.
	 *
	 * @return
	 */
	int getFragmentBeginIndex();

	/**
	 * Returns the character-based index within the surrounding markable of
	 * this fragment that denotes the actual end of the fragment itself.
	 *
	 * @return
	 */
	int getFragmentEndIndex();

	// Modification methods

	/**
	 * Changes the begin offset of the fragment to the new {@code index}.
	 *
	 * @param index
	 * @throws IndexOutOfBoundsException if the {@code index} argument is negative
	 * or exceeds either the current end index or the maximal possible index as
	 * specified by the textual size of the hosting markable
	 */
	void setFragmentBeginIndex(int index);

	/**
	 * Changes the end offset of the fragment to the new {@code index}.
	 *
	 * @param index
	 * @throws IndexOutOfBoundsException if the {@code index} argument is negative
	 * or less than the current end index or if it exceeds the maximal possible
	 * index as specified by the textual size of the hosting markable
	 */
	void setFragmentEndIndex(int index);
}
