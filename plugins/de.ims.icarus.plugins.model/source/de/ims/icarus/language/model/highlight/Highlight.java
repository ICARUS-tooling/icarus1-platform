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
package de.ims.icarus.language.model.highlight;

import java.awt.Color;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Markable;

/**
 * Models a set of highlight informations for a given {@link Container}
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Highlight {

	/**
	 * Returns the container that contains legal {@code Markable} objects
	 * that can be passed to the various methods in this interface.
	 */
	Container getContainer();

	HighlightCursor getHighlightCursor();

	boolean isHighlighted(Markable markable);

	boolean isHighlighted(Markable markable, int layerIndex);

	Color getHighlightColor(Markable markable);

	Color getHighlightColor(Markable markable, int layerIndex);

	int getGroupId(Markable markable);

	int getGroupId(Markable markable, int layerIndex);
}
