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
package de.ims.icarus.resources;


/**
 * A more complex version of the simple {@link Localizer}
 * with the ability to store localization related information
 * independent from the objects to be localized. This is
 * particularly useful when objects that require localization
 * offer no storage for localization related data (a common
 * example are some AWT members or custom objects)
 * 
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public interface RegisteringLocalizer extends Localizer {
	
	/**
	 * Stores localization data for a certain {@code Object}.
	 * The nature and meaning of the data being stored is highly
	 * implementation specific.
	 * @param item the item that requires localization
	 * @param data the data to be used when localizing the given {@code Object}
	 */
	void register(Object item, Object data);
}