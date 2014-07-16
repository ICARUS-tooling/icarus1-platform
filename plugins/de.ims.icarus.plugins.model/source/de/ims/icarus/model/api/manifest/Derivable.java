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

import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface Derivable extends Manifest {

	/**
	 * Returns whether or not this {@code Derivable} is meant to be a template,
	 * i.e. an abstract base description that cannot be used to directly instantiate
	 * objects from.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isTemplate();

	/**
	 * Returns the namespace wide unique id of this template/derivable.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getId();

	/**
	 * If derived from another template, this method returns the object used for
	 * templating or {@code null} otherwise.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Derivable getTemplate();

	// Modification methods

	void setId(String id);

	void setIsTemplate(boolean isTemplate);

//	/**
//	 * Changes the template used to derive content from.
//	 *
//	 * @param template
//	 */
//	void setTemplate(Derivable template);

	void setTemplateId(String templateId);
}
