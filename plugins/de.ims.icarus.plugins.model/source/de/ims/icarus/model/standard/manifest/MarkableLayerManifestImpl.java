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
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerManifestImpl extends AbstractLayerManifest<MarkableLayerManifest> implements MarkableLayerManifest {

	private final List<ContainerManifest> containerManifests = new ArrayList<>();

	private TargetLayerManifest boundaryLayerManifest;

	/**
	 * @param manifestSource
	 * @param registry
	 * @param layerGroupManifest
	 */
	public MarkableLayerManifestImpl(ManifestSource manifestSource,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestSource, registry, layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractLayerManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		if(boundaryLayerManifest!=null) {
			ModelXmlUtils.writeTargetLayerManifestElement(serializer, TAG_BOUNDARY_LAYER, boundaryLayerManifest);
		}

		for(ContainerManifest containerManifest : containerManifests) {
			containerManifest.writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_MARKABLE_LAYER;
	}

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
		int depth = containerManifests.size();
		if(depth==0 && hasTemplate()) {
			depth = getTemplate().getContainerDepth();
		}
		return depth;
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
		ContainerManifest result = null;
		if(!containerManifests.isEmpty()) {
			result = containerManifests.get(level);
		} else if(hasTemplate()) {
			result = getTemplate().getContainerManifest(level);
		}

		if(result==null)
			throw new IndexOutOfBoundsException("No container manifest available for level: "+level); //$NON-NLS-1$

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#indexOfContainerManifest(de.ims.icarus.model.api.manifest.ContainerManifest)
	 */
	@Override
	public int indexOfContainerManifest(ContainerManifest containerManifest) {
		if (containerManifest == null)
			throw new NullPointerException("Invalid containerManifest"); //$NON-NLS-1$

		int index = containerManifests.indexOf(containerManifest);

		if(index==-1 && containerManifests.isEmpty() && hasTemplate()) {
			index = getTemplate().indexOfContainerManifest(containerManifest);
		}

		return index;
	}

	public void addContainerManifest(ContainerManifest containerManifest) {
		if (containerManifest == null)
			throw new NullPointerException("Invalid containerManifest"); //$NON-NLS-1$

		containerManifests.add(containerManifest);
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
//	@Override
	public TargetLayerManifest setBoundaryLayerId(String boundaryLayerId) {
		checkAllowsTargetLayer();
		TargetLayerManifest manifest = new TargetLayerManifestImpl(boundaryLayerId);
		boundaryLayerManifest = manifest;
		return manifest;
	}
}
