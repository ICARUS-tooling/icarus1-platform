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
import de.ims.icarus.model.xml.ModelXmlElement;


/**
 * Layer groups describe logical
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LayerGroupManifest extends ModelXmlElement {

	public static final boolean DEFAULT_INDEPENDENT_VALUE = false;

	@AccessRestriction(AccessMode.READ)
	ContextManifest getContextManifest();

	@AccessRestriction(AccessMode.READ)
	List<LayerManifest> getLayerManifests();

	@AccessRestriction(AccessMode.READ)
	MarkableLayerManifest getPrimaryLayerManifest();

	/**
	 * Signals that the layers in this group do not depend on external data hosted in other
	 * groups within the same context. Note that this does <b>not</b> mean the layers are totally
	 * independent of content that resides in another context! Full independence is given when
	 * both this method and {@link ContextManifest#isIndependentContext()} of the describing
	 * manifest of the surrounding context return {@code true}.
	 * <p>
	 * Default is {@code false}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	boolean isIndependent();

	@AccessRestriction(AccessMode.READ)
	String getName();

	@AccessRestriction(AccessMode.READ)
	LayerManifest getLayerManifest(String id);

	/**
	 * Tests whether this {@code LayerGroupManifest} equals the given {@code Object} {@code o}.
	 * Two {@code LayerGroupManifest} instances are considered equal if they have the same name
	 * attribute as returned by {@link #getName()}.
	 *
	 * @param obj
	 * @return
	 */
	@Override
	boolean equals(Object o);

	// Modification methods

//	void setContextManifest(ContextManifest contextManifest);

//	void addLayerManifest(LayerManifest layerManifest);

//	void removeLayerManifest(LayerManifest layerManifest);

//	void setPrimaryLayerManifest(MarkableLayerManifest layerManifest);

//	void setIndependent(boolean isIndependent);

//	void setName(String name);
}
