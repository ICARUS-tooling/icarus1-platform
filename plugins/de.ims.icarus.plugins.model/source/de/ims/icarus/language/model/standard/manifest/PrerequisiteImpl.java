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

import de.ims.icarus.language.model.manifest.Prerequisite;
import de.ims.icarus.util.strings.StringUtil;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class PrerequisiteImpl implements Prerequisite {

	private final String layerId;
	private final String typeId;
	private final String contextId;
	private final String alias;

	private final int hash;

	public PrerequisiteImpl(String layerId, String typeId, String contextId, String alias) {
		if(layerId==null && typeId==null)
			throw new NullPointerException("Invalid layer and type id"); //$NON-NLS-1$

		this.layerId = layerId;
		this.contextId = contextId;
		this.alias = alias;
		this.typeId = typeId;

		int hash = 1;
		if(layerId!=null) {
			hash *= layerId.hashCode()+1;
		}
		if(typeId!=null) {
			hash *= typeId.hashCode()+1;
		}
		if(contextId!=null) {
			hash *= contextId.hashCode()+1;
		}
		if(alias!=null) {
			hash += alias.hashCode()+1;
		}

		this.hash = hash;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Prerequisite#getLayerId()
	 */
	@Override
	public String getLayerId() {
		return layerId;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Prerequisite#getTypeId()
	 */
	@Override
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Prerequisite#getContextId()
	 */
	@Override
	public String getContextId() {
		return contextId;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Prerequisite#getAlias()
	 */
	@Override
	public String getAlias() {
		return alias;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Prerequisite) {
			String name = ((Prerequisite) obj).getLayerId();
			return name!=null && layerId.equals(name);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Prerequisite["); //$NON-NLS-1$
		if(contextId!=null) {
			sb.append("context-id=").append(contextId).append(' '); //$NON-NLS-1$
		}
		if(layerId!=null) {
			sb.append("layer-id=").append(layerId).append(' '); //$NON-NLS-1$
		}
		if(typeId!=null) {
			sb.append("type-id=").append(typeId).append(' '); //$NON-NLS-1$
		}
		if(alias!=null) {
			sb.append("alias=").append(alias).append(' '); //$NON-NLS-1$
		}
		StringUtil.trim(sb);
		sb.append(']');

		return sb.toString();
	}
}
