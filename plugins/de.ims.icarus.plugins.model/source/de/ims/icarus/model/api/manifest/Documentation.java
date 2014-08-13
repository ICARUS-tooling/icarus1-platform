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

import java.util.List;

import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.model.util.types.Url;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface Documentation extends ModifiableIdentity, ModelXmlElement, ModelXmlHandler {

	@AccessRestriction(AccessMode.READ)
	String getContent();

	@AccessRestriction(AccessMode.READ)
	List<Resource> getResources();

	// Modification methods

//	void setTarget(Documentable documentable);
//
//	void setContent(String content);
//
//	void addResource(Resource resource);
//
//	void removeResource(Resource resource);

	@AccessControl(AccessPolicy.DENY)
	public interface Resource extends ModifiableIdentity {

		@AccessRestriction(AccessMode.READ)
		Url getUrl();
	}
}
