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
package de.ims.icarus.language.model.manifest;

import java.util.List;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LayerManifest extends Manifest {

	/**
	 * Returns a list of prerequisites describing other layers a corpus
	 * has to host in order for the new layer to be operational. If this
	 * layer does not depend on other layers the returned list is empty.
	 * 
	 * @return
	 */
	List<Prerequisite> getPrerequisites();

	/**
	 * Abstract description of a layer object this layer depends on.
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	interface Prerequisite {

		/**
		 * Returns the name of the target layer or {@code null} if an exact name match
		 * is not required.
		 * 
		 * @return
		 */
		String getLayerName();

		/**
		 * If this layer only requires <i>some</i> layer of a certain type to be present
		 * this method provides the mechanics to tell this. When the returned value is
		 * {@code non-null} it is considered to be the exact name of a previously
		 * defined layer type.
		 * 
		 * @return
		 */
		String getTypeName();
	}
}
