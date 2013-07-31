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
package de.ims.icarus.ui.helper;



/**
 * Serves as a kind of "flag" to mark objects that can be "configured"
 * by the user in some way. The {@link #openConfig()} method serves as
 * delegate to access the real configuration. This does not have to be
 * the default {@code ConfigDialog} implementation but can be something
 * as simple as a little {@code Dialog} with some input components like
 * text-fields or check-boxes. It is recommended that any class that
 * wishes to be presented to the user as part of some UI should implement
 * this interface in case it features configuration possibilities.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Configurable {

	/**
	 * Opens and presents the user the configuration interface associated
	 * with this object. The nature of this "interface" is implementation
	 * specific and is not restricted to the default {@code ConfigDialog}
	 * used to access a {@code ConfigStorage}.
	 */
	void openConfig();
}
