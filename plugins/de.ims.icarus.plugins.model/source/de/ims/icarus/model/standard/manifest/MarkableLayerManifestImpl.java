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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerManifestImpl extends AbstractLayerManifest<MarkableLayerManifest> implements MarkableLayerManifest {

	private List<ContainerManifest> containerManifests;

	private TargetLayerManifest boundaryLayerManifest;

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.MARKABLE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getContainerDepth()
	 */
	@Override
	public int getContainerDepth() {
//		if(containerManifests==null)
//			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests==null ? 0 : containerManifests.size();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		return getContainerManifest(0);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getContainerManifest(int)
	 */
	@Override
	public ContainerManifest getContainerManifest(int level) {
		if(containerManifests==null)
			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests.get(level);
	}

	public void addContainerManifest(ContainerManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(containerManifests==null) {
			containerManifests = new ArrayList<>(3);
		}

		containerManifests.add(manifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getBoundaryLayerManifest()
	 */
	@Override
	public TargetLayerManifest getBoundaryLayerManifest() {
		return boundaryLayerManifest;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	public void setBoundaryLayerManifest(TargetLayerManifest boundaryLayerManifest) {
		if (boundaryLayerManifest == null)
			throw new NullPointerException("Invalid boundaryLayerManifest"); //$NON-NLS-1$

		this.boundaryLayerManifest = boundaryLayerManifest;
	}
}
