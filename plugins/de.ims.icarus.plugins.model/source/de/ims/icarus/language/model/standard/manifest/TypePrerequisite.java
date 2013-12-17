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
public class TypePrerequisite implements LayerManifest.Prerequisite {

	private final String typeId;

	public TypePrerequisite(String typeId) {
		if(typeId==null)
			throw new NullPointerException("Invalid type id"); //$NON-NLS-1$
		if(typeId.isEmpty())
			throw new IllegalArgumentException("Empty type id"); //$NON-NLS-1$

		this.typeId = typeId;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest.Prerequisite#getLayerId()
	 */
	@Override
	public String getLayerId() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.LayerManifest.Prerequisite#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return typeId.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LayerManifest.Prerequisite) {
			String type = ((LayerManifest.Prerequisite) obj).getTypeId();
			return type!=null && typeId.equals(type);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Required type-id: "+typeId; //$NON-NLS-1$;
	}

}
