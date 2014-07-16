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
package de.ims.icarus.model.api.manifest;

import de.ims.icarus.model.api.StructureType;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface StructureManifest extends ContainerManifest {

	public static final boolean DEFAULT_MULTI_ROOT_VALUE = false;

	/**
	 * Returns the type of this structure
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	StructureType getStructureType();

	@AccessRestriction(AccessMode.READ)
	boolean isMultiRootAllowed();

	// Modification methods

//	void setStructureType(StructureType structureType);

//	void setMultiRootAllowed(boolean multiRootAllowed);
}
