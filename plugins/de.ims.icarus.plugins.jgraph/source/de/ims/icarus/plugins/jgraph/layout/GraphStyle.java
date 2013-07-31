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
package de.ims.icarus.plugins.jgraph.layout;


import com.mxgraph.view.mxStylesheet;

import de.ims.icarus.util.Installable;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface GraphStyle extends Installable {

	/**
	 * Generates a {@code mxStylesheet} to be used for the given
	 * graph. It is up to the {@code GraphStyle} implementation
	 * whether all possible styles should be defined in the returned
	 * stylesheet. Typical implementations will define some general
	 * base styles and do the "decoration" when asked to fetch the
	 * style for a particular cell.
	 */
	mxStylesheet createStylesheet(GraphOwner owner, Options options);
	
	/**
	 * Generate a style string to be used for the given cell.
	 * <p>
	 * Note that the {@code GraphStyle} implementation itself is not
	 * meant to set the style on a cell directly! It is merely a
	 * source for style definitions and responsible for fetching
	 * the right style.
	 */
	String getStyle(GraphOwner owner, Object cell, Options options);
}
