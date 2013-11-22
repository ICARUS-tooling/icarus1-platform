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
package de.ims.icarus.language.model.meta;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Markable;

/**
 * A helper structure to allow to lookup the higher level markables 
 * that reference certain lower markable objects. Imagine for example
 * a layer describing tokens in a corpus and a layer on top of that
 * which groups the tokens into sentences. Since the containers hosting
 * the actual markable objects are placed in distinct layers there is
 * no natural way of obtaining the sentence a certain token is part of.
 * The {@code ReverseLookup} interface provides the lookup functionality
 * by mapping the tokens to their individual sentence object.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ReverseLookup {

	/**
	 * Returns the container that hosts the markable objects
	 * which are referenced by the markables in the container
	 * returned by {@link #getOverlay()}.
	 * @return
	 */
	Container getBase();
	
	/**
	 * Returns the container that builds his markables on top of
	 * the markable objects in the base container as returned by
	 * {@link #getOverlay()}.
	 * @return
	 */
	Container getOverlay();
	
	/**
	 * Returns the markable in the overly container that references 
	 * the underlying markable {@code m}.
	 * @param m
	 * @return
	 */
	Markable lookup(Markable m);
}
