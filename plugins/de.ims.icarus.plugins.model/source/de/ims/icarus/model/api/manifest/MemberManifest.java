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
 * A manifest is a kind of descriptor for parts of a corpus.
 * It stores information relevant to localization and identification
 * of the item it describes. Manifests for the most part are immutable
 * storage objects created by the model framework. They normally derive
 * from a static xml definition and the only thing the user can modify
 * in certain cases is the identifier used to present them in the GUI.
 *
 * When saving the current state of a corpus, the framework converts the
 * manifests back into a physical xml-based representation.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface MemberManifest extends ModifiableIdentity, ModifiableManifest {

	/**
	 * Returns the {@code type} of this manifest, i.e. that is
	 * what kind of member in a corpus it describes. If type-specific
	 * behavior is modeled, one should always use this method rather than
	 * doing multiple {@code instanceof} checks.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ManifestType getManifestType();

//	/**
//	 * Returns the private id of this manifest that is shared between all manifests
//	 * derived from the same template. This id can be used to resolve dependencies
//	 * defined in templates to actual public ids of the instantiated members in a
//	 * corpus. Note that unlike {@link #getId()} the uniqueness of raw ids is enforced
//	 * on the context level, so that no two layers within the same context share a
//	 * common raw id. This is a crucial requirement for id resolution, since ambiguity
//	 * of raw ids cannot be resolved by the framework!
//	 *
//	 * @see #getId()
//	 */
//	String getRawId();

	// Modification methods
}
