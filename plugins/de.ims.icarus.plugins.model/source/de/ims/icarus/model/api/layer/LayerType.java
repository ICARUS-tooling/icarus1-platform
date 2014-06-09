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
package de.ims.icarus.model.api.layer;

import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.util.id.Identity;

/**
 * Implements a shared type descriptor for {@code Layer} objects. It is used to
 * group layers according to an abstract description of their content. Besides
 * serving as a mere identifier to that abstract description, a {@code LayerType}
 * optionally provides a {@link LayerManifest} that contains further specifications
 * on how the content might be structured or other informations. Each layer type
 * is globally identifiably by its unique id.
 *
 * @author Markus Gärtner
 * @version $Id$
 * @see LayerManifest
 *
 */
public interface LayerType extends Identity {

	/**
	 * Returns the shared {@code LayerManifest} that further describes layers of
	 * this type or {@code null} if this type only serves as a identifier without
	 * additional content restrictions.
	 * @return
	 */
	LayerManifest getSharedManifest();
}
