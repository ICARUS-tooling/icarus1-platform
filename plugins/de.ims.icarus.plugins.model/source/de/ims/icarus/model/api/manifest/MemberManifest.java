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

import java.util.Set;

import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.model.xml.ModelXmlElement;


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
public interface MemberManifest extends ModifiableIdentity, Manifest, ModelXmlElement {

	@AccessRestriction(AccessMode.READ)
	Documentation getDocumentation();

	/**
	 * Returns the manifest that describes possible options the
	 * user can assign to this manifest. If the manifest does not
	 * support additional properties assignable by the user, this
	 * method returns {@code null}.
	 *
	 * @return the manifest describing options for this manifest
	 * or {@code null}
	 */
	@AccessRestriction(AccessMode.READ)
	OptionsManifest getOptionsManifest();

	/**
	 * Returns the property assigned to this manifest for the given
	 * name. If their is no property with the given name available
	 * this method should return {@code null}. Note that multi-value
	 * properties will typically return a collection of values.
	 *
	 * @param name The name of the property in question
	 * @return The value of the property with the given name or {@code null}
	 * if no such property exists.
	 */
	@AccessRestriction(AccessMode.READ)
	Object getProperty(String name);

	/**
	 * Returns a {@link Set} view of all the available property names
	 * in this manifest. If there are no properties in the manifest
	 * available then this method should return an empty {@code Set}!
	 * <p>
	 * The returned {@code Set} should be immutable.
	 *
	 * @return A {@code Set} view on all the available property names
	 * for this manifest or the empty {@code Set} if this manifest does
	 * not contain any properties.
	 */
	@AccessRestriction(AccessMode.READ)
	Set<String> getPropertyNames();

	// Modification methods

	/**
	 * Changes the value of the property specified by {@code name} to
	 * the new {@code value}. Note that only for multi-value properties
	 * it is allowed to pass collections of values!
	 *
	 * @param name The name of the property to be changed
	 * @param value The new value for the property, allowed to be {@code null}
	 * if stated so in the {@code OptionsManifest} for this manifest
	 * @throws NullPointerException if the {@code name} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code value} argument does not
	 * fulfill the contract described in the {@code OptionsManifest} of this
	 * manifest.
	 * @throws UnsupportedOperationException if the manifest does not declare
	 * any properties the user can modify.
	 */
	void setProperty(String name, Object value);

	void setOptionsManifest(OptionsManifest optionsManifest);

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
