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


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface MarkableLayerManifest extends LayerManifest {

	/**
	 * Returns {@code true} if the layer contains structures as members of 
	 * its top-level container. Note that if the layer contains a single big
	 * structure as its only markable this structure has still to be contained
	 * within a wrapper container since {@code Structure} does not extend
	 * {@code Container}!
	 * <p>
	 * A return value of {@code false} does not prevent the layer from containing
	 * a mix of structural and bare markable items or structure objects in some 
	 * deeper level. This method is intended to be a hint for utility frameworks
	 * to better decide on what visualizations and or access methods to use for 
	 * this layer.
	 * 
	 * @return {@code true} if this layer is meant as a container for structure
	 * informations and wishes to be handled as such.
	 */
	boolean isStructureLayer();
	
	/**
	 * Returns the number of nested containers and/or structures within this
	 * layer.
	 * <p>
	 * Note that the returned value is always at least {@code 1}.
	 * @return
	 */
	int getContainerDepth();
	
	ContextManifest getRootContainerManifest();
	
	ContainerManifest getContainerManifest(int level);
}
