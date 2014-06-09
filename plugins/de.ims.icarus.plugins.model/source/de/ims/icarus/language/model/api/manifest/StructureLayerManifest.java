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




/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface StructureLayerManifest extends MarkableLayerManifest {

	/**
	 * Returns the manifest for the members of the top-level
	 * container in this layer. This is effectively the same as calling
	 * {@link #getContainerManifest(int)} with a {@code level} value
	 * of {@code 1} and casting the result to {@code StructureManifest}.
	 * Note that a structure layer always contains a regular container
	 * as root of its container hierarchy. Only on the subsequent levels
	 * the structures themselves are hosted!
	 *
	 * @return
	 * @see #getRootContainerManifest()
	 * @see #getContextManifest()
	 */
	StructureManifest getStructureManifest();
}
