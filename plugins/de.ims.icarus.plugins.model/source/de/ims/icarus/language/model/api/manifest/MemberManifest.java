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
package de.ims.icarus.language.model.api.manifest;

import java.util.Collection;
import java.util.Set;

import de.ims.icarus.language.model.api.NamedCorpusMember;
import de.ims.icarus.util.id.Identity;

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
public interface MemberManifest extends Identity, Derivable {

	/**
	 * Returns the {@code type} of this manifest, i.e. that is
	 * what kind of member in a corpus it describes. If type-specific
	 * behavior is modeled, one should always use this method rather than
	 * doing multiple {@code instanceof} checks.
	 *
	 * @return
	 */
	ManifestType getManifestType();

	/**
	 * Returns the {@code Implementation} object that holds information
	 * about foreign implementations that should be used to instantiate
	 * the corpus member this manifest describes. If no preferred implementation
	 * is specified by the user, this method returns {@code null}.
	 *
	 * @return
	 */
	Implementation getImplementation();

	/**
	 * Returns the public id of this manifest that can be used to
	 * uniquely identify it. Note that uniqueness of public ids is
	 * enforced on the corpus level. So for example there cannot be
	 * two layers within a single corpus that share the same public id,
	 * even when they are originating from different contexts.
	 *
	 * @see de.ims.icarus.util.id.Identity#getId()
	 * @see #getRawId()
	 */
	@Override
	String getId();

	/**
	 * Returns the private id of this manifest that is shared between all manifests
	 * derived from the same template. This id can be used to resolve dependencies
	 * defined in templates to actual public ids of the instantiated members in a
	 * corpus. Note that unlike {@link #getId()} the uniqueness of raw ids is enforced
	 * on the context level, so that no two layers within the same context share s
	 * common raw id. This is a crucial requirement for id resolution, since ambiguity
	 * of raw ids cannot be resolved by the framework!
	 *
	 * @see #getId()
	 */
	String getRawId();

	/**
	 * Allows for localization of corpus related components.
	 * The returned name is not required to be equal to the
	 * result of a {@link NamedCorpusMember#getName()} call.
	 *
	 * @return The (optionally) localized name of the task
	 * this manifest describes.
	 */
	@Override
	String getName();

	/**
	 * Returns a more detailed description of the task performed
	 * by instances of this manifest.
	 * <p>
	 * This is an optional method.
	 *
	 * @return Returns the optional description of this manifest
	 */
	@Override
	String getDescription();

	/**
	 * Returns the manifest that describes possible options the
	 * user can assign to this manifest. If the manifest does not
	 * support additional properties assignable by the user, this
	 * method returns {@code null}.
	 *
	 * @return the manifest describing options for this manifest
	 * or {@code null}
	 */
	OptionsManifest getOptionsManifest();

	/**
	 * Returns the property assigned to this manifest for the given
	 * name. If their is no property with the given name available
	 * this method should return {@code null}.
	 *
	 * @param name The name of the property in question
	 * @return The value of the property with the given name or {@code null}
	 * if no such property exists.
	 */
	Object getProperty(String name);

	Collection<Object> getProperties(String name);

	/**
	 * Changes the value of the property specified by {@code name} to
	 * the new {@code value}.
	 *
	 * @param name The name of the property to be changed
	 * @param value The new value for the property, allowed to be {@code null}
	 * if stated so in the {@code OptionsManifest} for this manifest
	 * @throws NullPointerException if the {@code name} argument is {@code null}
	 * @throws IllegalArgumentException if the {@code value} argument does not
	 * fulfill the contract described in the {@code OptionsManifest} of this
	 * manifest.
	 * @throws UnsupportedOperationException if the manifest does not declare
	 * any properties the user can modify
	 */
	void setProperty(String name, Object value);

	void setProperties(String name, Collection<Object> values);

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
	Set<String> getPropertyNames();
}
