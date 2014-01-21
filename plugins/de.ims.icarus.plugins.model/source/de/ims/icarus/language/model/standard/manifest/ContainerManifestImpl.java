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

import de.ims.icarus.language.model.ContainerType;
import de.ims.icarus.language.model.manifest.ContainerManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.manifest.MarkableLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContainerManifestImpl extends AbstractManifest<ContainerManifest> implements ContainerManifest {

	private final ContainerManifest parentManifest;
	private final MarkableLayerManifest layerManifest;

	private ContainerManifest elementManifest;
	private ContainerType containerType;

	public ContainerManifestImpl(MarkableLayerManifest layerManifest) {
		if (layerManifest == null)
			throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$

		this.layerManifest = layerManifest;
		this.parentManifest = null;
	}

	public ContainerManifestImpl(ContainerManifest parentManifest) {
		if (parentManifest == null)
			throw new NullPointerException("Invalid parentManifest"); //$NON-NLS-1$

		this.parentManifest = parentManifest;
		this.layerManifest = null;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTAINER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContainerManifest#getLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getLayerManifest() {
		return layerManifest==null ? parentManifest.getLayerManifest() : layerManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContainerManifest#getParentManifest()
	 */
	@Override
	public ContainerManifest getParentManifest() {
		return parentManifest;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContainerManifest#getElementManifest()
	 */
	@Override
	public ContainerManifest getElementManifest() {
		ContainerManifest elementManifest = this.elementManifest;

		if(elementManifest==null && hasTemplate()) {
			elementManifest = getTemplate().getElementManifest();
		}

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
	 * @see de.ims.icarus.language.model.manifest.ContainerManifest#getContainerType()
	 */
	@Override
	public ContainerType getContainerType() {
		ContainerType containerType = this.containerType;

		if(containerType==null && hasTemplate()) {
			containerType = getTemplate().getContainerType();
		}

		return containerType;
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContainerManifest#setContainerType(de.ims.icarus.language.model.ContainerType)
	 */
	@Override
	public void setContainerType(ContainerType containerType) {
		if (containerType == null)
			throw new NullPointerException("Invalid containerType"); //$NON-NLS-1$

		this.containerType = containerType;
	}

}
