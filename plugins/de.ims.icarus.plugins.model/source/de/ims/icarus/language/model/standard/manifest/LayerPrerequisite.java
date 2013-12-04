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
package de.ims.icarus.language.model.standard.manifest;

import de.ims.icarus.language.model.manifest.LayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LayerPrerequisite implements LayerManifest.Prerequisite {
	
	private final String layerName;

	public LayerPrerequisite(String layerName) {
		if(layerName==null)
			throw new NullPointerException("Invalid layer name"); //$NON-NLS-1$
		if(layerName.isEmpty())
			throw new IllegalArgumentException("Empty layer name"); //$NON-NLS-1$
		
		this.layerName = layerName;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest.Prerequisite#getLayerName()
	 */
	@Override
	public String getLayerName() {
		return layerName;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest.Prerequisite#getTypeName()
	 */
	@Override
	public String getTypeName() {
		return null;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return layerName.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LayerManifest.Prerequisite) {
			String name = ((LayerManifest.Prerequisite) obj).getLayerName();
			return name!=null && layerName.equals(name);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Required layer-name: "+layerName; //$NON-NLS-1$;
	}

}
