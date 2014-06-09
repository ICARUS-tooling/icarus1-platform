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

import java.util.List;


/**
 * Layer groups describe logical
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LayerGroupManifest {

	ContextManifest getContextManifest();

	List<LayerManifest> getLayerManifests();

	MarkableLayerManifest getPrimaryLayerManifest();

	/**
	 * Signals that the layers in this group do not depend on external data hosted in other
	 * groups within the same context. Note that this does <b>not</b> mean the layers are totally
	 * independent of content that resides in another context! Full independence is given when
	 * both this method and {@link ContextManifest#isIndependentContext()} of the describing
	 * manifest of the surrounding context return {@code true}.
	 *
	 * @return
	 */
	boolean isIndependent();

	String getName();
}
