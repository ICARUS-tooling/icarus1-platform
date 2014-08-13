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

import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface DriverManifest extends MemberManifest {

	/**
	 * Returns the manifest that specifies the actual driver implementation.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ImplementationManifest getImplementationManifest();

	/**
	 * Returns manifests describing all the indices that should be created for this
	 * context.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	List<IndexManifest> getIndexManifests();

	/**
	 * Returns a hint on which type of resources the driver is depending to access
	 * corpus data.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	LocationType getLocationType();

	/**
	 *  For live driver manifests this method returns the manifest describing the
	 *  surrounding {@code Context}. For templates the return value is {@code null}.
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	ContextManifest getContextManifest();

	@AccessRestriction(AccessMode.READ)
	List<ModuleManifest> getModuleManifests();

	@AccessRestriction(AccessMode.READ)
	List<ModuleSpec> getModuleSpecs();

	ModuleSpec getModuleSpec(String specId);

	ModuleManifest getModuleManifest(String moduleId);

	// Modification methods

//	void setImplementationManifest(ImplementationManifest implementationManifest);
//
//	void addIndexManifest(IndexManifest indexManifest);
//
//	void removeIndexManifest(IndexManifest indexManifest);
//
//	void setLocationType(LocationType locationType);

	/**
	 * Describes a module this driver manifest is depending on. A driver can
	 * contain an arbitrary number of {@code ModuleSpec} declarations. Each
	 * module represents a pluggable part of the driver that (optionally)
	 * can be customized by the user.
	 * <p>
	 * Note that {@code ModuleSpec} ids are required to be unique within a single
	 * driver manifest's scope. Therefore declaring module specs in a derived manifest
	 * effectively shadows previous declarations in the original template!
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface ModuleSpec extends Identity {

		public static final boolean DEFAULT_IS_OPTIONAL = false;
		public static final boolean DEFAULT_IS_CUSTOMIZABLE = false;

		@AccessRestriction(AccessMode.READ)
		DriverManifest getDriverManifest();

		/**
		 * Specifies whether or not the described module is optional.
		 * <p>
		 * The default is {@value #DEFAULT_IS_OPTIONAL}.
		 *
		 * @return
		 *
		 * @see #DEFAULT_IS_OPTIONAL
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isOptional();

		/**
		 * Specifies whether or not the described module can be customized
		 * by the user. When a module is customizable it will show up in the
		 * options dialog for a driver. Non-customizable modules are final
		 * upon their manifest declaration. For example in the specification
		 * of a certain format the module describing the file connector will
		 * probably be non-customizable, while additional language specific modules
		 * will be left for the user to customize.
		 * <p>
		 * The default is {@value #DEFAULT_IS_CUSTOMIZABLE}.
		 *
		 * @return
		 *
		 * @see #DEFAULT_IS_CUSTOMIZABLE
		 */
		@AccessRestriction(AccessMode.READ)
		boolean isCustomizable();

		/**
		 * Specifies an extension-point from which to load connected extensions. Those
		 * extensions then describe the legal implementations that can be used for this module.
		 * <p>
		 * Note that when no
		 *
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		String getExtensionPointUid();
	}

	/**
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	@AccessControl(AccessPolicy.DENY)
	public interface ModuleManifest extends MemberManifest {

		@AccessRestriction(AccessMode.READ)
		DriverManifest getDriverManifest();

		/**
		 * Returns the {@link ModuleSpec} that describes this module.
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ModuleSpec getModuleSpec();

		/**
		 * Returns the manifest that specifies the implementation of this module.
		 * @return
		 */
		@AccessRestriction(AccessMode.READ)
		ImplementationManifest getImplementationManifest();
	}
}
