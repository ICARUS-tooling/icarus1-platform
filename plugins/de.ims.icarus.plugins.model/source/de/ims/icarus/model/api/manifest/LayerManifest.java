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

import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerType;

/**
 * A {@code LayerManifest} describes a single {@link Layer} in a corpus and
 * defines an optional set of prerequisites that have to be met for the layer
 * to work properly.
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LayerManifest extends MemberManifest {

	ContextManifest getContextManifest();

	/**
	 * Returns the group manifest this layer is a part of.
	 *
	 * @return
	 */
	LayerGroupManifest getGroupManifest();

	/**
	 * Returns the optional layer type that acts as another abstraction layer
	 * to unify layers that share a common content structure. Note that all
	 * layer type instances are globally unique and are shared between all the
	 * layers of that type.
	 *
	 * @return
	 */
	LayerType getLayerType();

	/**
	 * Returns the list of resolved base layers for this layer manifest.
	 * @return
	 */
	List<TargetLayerManifest> getBaseLayerManifests();

	/**
	 * Models a resolved dependency on the layer level. A target layer may either be
	 * a local layer, hosted within the same context, or a foreign layer that has been
	 * resolved by means of binding a prerequisite manifest declaration to a layer manifest.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public interface TargetLayerManifest {

		/**
		 * Returns the source layer manifest for the dependency this manifest describes
		 * (that is the layer actually hosting a {@code TargetLayerManifest}).
		 *
		 * @return
		 */
		LayerManifest getLayerManifest();

		/**
		 * When the target layer resides in a foreign context and was resolved using
		 * a prerequisite manifest, this method returns the used prerequisite. In the
		 * case of a local layer being targeted, the return value is {@code null}.
		 * @return
		 */
		ContextManifest.PrerequisiteManifest getPrerequisite();

		/**
		 * Returns the actual target layer manifest this manifest refers to. Note that the
		 * return type is chosen to be the general {@link LayerManifest} class instead of the
		 * {@link MarkableLayerManifest} usually used for base or boundary layer declarations.
		 * This is so that {@link FragmentLayerManifest}s do not have to declare another linking
		 * manifest to account for their value layer declaration. The actually required type of
		 * layer should be concluded from the context in which the target layer is to be resolved.
		 *
		 * @return
		 */
		LayerManifest getResolvedLayerManifest();
	}
}
