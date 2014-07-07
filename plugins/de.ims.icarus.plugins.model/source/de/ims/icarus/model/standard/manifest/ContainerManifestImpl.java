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
package de.ims.icarus.model.standard.manifest;

import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.util.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContainerManifestImpl extends AbstractMemberManifest<ContainerManifest> implements ContainerManifest {

	private ContainerManifest parentManifest;
	private MarkableLayerManifest layerManifest;

	private ContainerManifest elementManifest;
	private ContainerType containerType;

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractMemberManifest#readTemplate(de.ims.icarus.model.api.manifest.MemberManifest)
	 */
	@Override
	protected void readTemplate(ContainerManifest template) {
		super.readTemplate(template);

		if(containerType==null) {
			containerType = template.getContainerType();
		}
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getLayerManifest() {
		return layerManifest==null ? parentManifest.getLayerManifest() : layerManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getParentManifest()
	 */
	@Override
	public ContainerManifest getParentManifest() {
		return parentManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getElementManifest()
	 */
	@Override
	public ContainerManifest getElementManifest() {
		return elementManifest;
	}

	/**
	 * @param elementManifest the elementManifest to set
	 */
	public void setElementManifest(ContainerManifest elementManifest) {
		if (elementManifest == null)
			throw new NullPointerException("Invalid elementManifest"); //$NON-NLS-1$

		this.elementManifest = elementManifest;
	}

	/**
	 * @param parentManifest the parentManifest to set
	 */
	public void setParentManifest(ContainerManifest parentManifest) {
		if (parentManifest == null)
			throw new NullPointerException("Invalid parentManifest"); //$NON-NLS-1$

		this.parentManifest = parentManifest;
	}

	/**
	 * @param layerManifest the layerManifest to set
	 */
	public void setLayerManifest(MarkableLayerManifest layerManifest) {
		if (layerManifest == null)
			throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$

		this.layerManifest = layerManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContainerManifest#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		return containerType==null ? ContainerType.LIST : containerType;
	}

	public void setContainerType(ContainerType containerType) {
		if (containerType == null)
			throw new NullPointerException("Invalid containerType"); //$NON-NLS-1$

		this.containerType = containerType;
	}

	/**
	 * @see de.ims.icarus.model.api.standard.manifest.AbstractMemberManifest#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)) {
			return false;
		}

		if(obj instanceof ContainerManifest) {
			ContainerManifest other = (ContainerManifest) obj;
			return containerType==other.getContainerType()
					&& (ClassUtils.equals(elementManifest, other.getElementManifest()));
		}
		return false;
	}

}
