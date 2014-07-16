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
import de.ims.icarus.model.util.types.ValueType;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.util.id.Identity;

/**
 * Helper manifest (not describing a corpus member/entity of its own)
 * to specify possible properties the user can set on another manifest.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface OptionsManifest extends Derivable, ModelXmlElement {

	/**
	 * Returns the names of all available options for the target
	 * manifest. This {@code Set} is guaranteed to be non-null and
	 * non-empty.
	 *
	 * @return The names of all available options as a {@code Set}
	 */
	@AccessRestriction(AccessMode.READ)
	Set<String> getOptionNames();

	/**
	 * Returns a collection of dedicated identifiers for groups in this options manifest.
	 * Note that a group used as result of {@link #getOptionGroup(String)} is not required
	 * to have a matching identity implementation in the returned set of this method. The
	 * returned identifiers are merely an additional chunk of localization and/or visualization
	 * hints for user interfaces.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	Set<Identity> getGroupIdentifiers();

	@AccessRestriction(AccessMode.READ)
	Option getOption(String id);

	// Modification methods

//	void removeOption(String name);
//
//	void addOption(String name);
//
//	void setDefaultValue(String name, Object value);
//
//	void setValueType(String name, ValueType type);
//
//	void setName(String name, String value);
//
//	void setDescription(String name, String value);
//
//	void setOptionGroup(String name, String value);
//
//	void setSupportedValues(String name, ValueSet values);
//
//	void setSupportedRange(String name, ValueRange range);
//
//	void setPublished(String name, boolean published);
//
//	void setMultiValue(String name, boolean multiValue);
//
//	void addGroupIdentifier(Identity identifier);
//
//	void removeGroupIdentifier(Identity identifier);

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface Option extends Identity {

		public static final boolean DEFAULT_PUBLISHED_VALUE = true;
		public static final boolean DEFAULT_MULTIVALUE_VALUE = false;

		@Override
		@AccessRestriction(AccessMode.READ)
		String getId();

		/**
		 * Returns the default value for the property specified by the
		 * {@code name} argument
		 *
		 * @param name The {@code name} of the property for which the
		 * default value should be returned
		 * @return The default value for the specified property or {@code null}
		 * if the property has no default value assigned to it
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		Object getDefaultValue();

		/**
		 * Returns the type of the specified property. This method never
		 * returns {@code null}.
		 *
		 * @param name The {@code name} of the property for which the
		 * type should be returned
		 * @return The type for the specified property
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		ValueType getValueType();

		/**
		 * Returns a localized name string of the specified property, that
		 * is suitable for presentation in user interfaces.
		 *
		 * @param name The {@code name} of the property for which a
		 * localized name should be returned
		 * @return A localized name string for the specified property
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@Override
		@AccessRestriction(AccessMode.READ)
		String getName();

		/**
		 * Returns a localized description string of the specified property, that
		 * is suitable for presentation in user interfaces.
		 * <p>
		 * This is an optional method
		 *
		 * @param name The {@code name} of the property for which a
		 * localized description should be returned
		 * @return A localized description string for the specified property
		 * or {@code null} if there is no description available for it
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@Override
		@AccessRestriction(AccessMode.READ)
		String getDescription();

		/**
		 *
		 * @param name
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ValueSet getSupportedValues();

		/**
		 *
		 * @param name
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ValueRange getSupportedRange();

		/**
		 * To support graphical visualizations in their job of presenting configuration
		 * options, those options can be grouped together in logical collections. For reasons
		 * of simplicity there are no dedicated data structures to represent those groups, but
		 * the group's identifier is simply attached to an option as an id property. If further
		 * localization or additional complexity is required, the {@link #getGroupIdentifiers()}
		 * method can be used to obtain groups for this options manifest in the form of
		 * {@link Identity} implementations.
		 * <p>
		 * Note that is legal to assign groups to an option that have no dedicated identifier
		 * registered.
		 *
		 * @param name
		 * @return
		 * @see #getOptionNames()
		 */
		@AccessRestriction(AccessMode.READ)
		String getOptionGroup();

		/**
		 * Returns whether or not the option in question should be published
		 * to the user so he can modify it. Unpublished or <i>hidden</i> options
		 * are meant as a way of configuring implementations without allowing
		 * interference from the user.
		 *
		 * @param name
		 * @return
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isPublished();

		/**
		 * Returns whether an option is allowed to be assigned multiple values.
		 * This can be the case when the option in question presents the user a
		 * selective choice with several values.
		 *
		 * @param name
		 * @return
		 * @throws NullPointerException if the {@code name} argument
		 * is {@code null}
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isMultiValue();
	}
}
