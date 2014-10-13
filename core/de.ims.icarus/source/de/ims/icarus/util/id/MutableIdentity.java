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
package de.ims.icarus.util.id;

import javax.swing.Icon;

/**
 * Defines an {@code Identity} whose "appearance" fields can be changed
 * by foreign code. Note that the defining properties of an identity (i.e.
 * its owner and {@code id}) are <b>not</b> declared to be mutable! This
 * interface is intended for use cases where identity instances can be modified
 * by the user.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface MutableIdentity extends Identity {

	void setName(String name);

	void setDescription(String name);

	void setIdon(Icon icon);
}
