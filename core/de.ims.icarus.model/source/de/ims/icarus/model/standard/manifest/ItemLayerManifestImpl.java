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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.ItemLayerManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ItemLayerManifestImpl extends AbstractLayerManifest<ItemLayerManifest> implements ItemLayerManifest {

	private final List<ContainerManifest> containerManifests = new ArrayList<>();

	private TargetLayerManifest boundaryLayerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public ItemLayerManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return super.isEmpty() && boundaryLayerManifest==null && containerManifests.isEmpty();
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

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_MARKABLE_LAYER: {
			readAttributes(attributes);
		} break;

		case TAG_BOUNDARY_LAYER: {
			String boundaryLayerId = ModelXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			setBoundaryLayerId(boundaryLayerId);
		} break;

		case TAG_CONTAINER: {
			return new ContainerManifestImpl(manifestLocation, getRegistry(), this);
		}

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_MARKABLE_LAYER: {
			return null;
		}

		case TAG_BOUNDARY_LAYER: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_CONTAINER: {
			addContainerManifest((ContainerManifest) handler);
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
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
	 * @see de.ims.icarus.model.api.manifest.ItemLayerManifest#getContainerDepth()
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
	 * @see de.ims.icarus.model.api.manifest.ItemLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		return getContainerManifest(0);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ItemLayerManifest#getContainerManifest(int)
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
	 * @see de.ims.icarus.model.api.manifest.ItemLayerManifest#indexOfContainerManifest(de.ims.icarus.model.api.manifest.ContainerManifest)
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
	 * @see de.ims.icarus.model.api.manifest.ItemLayerManifest#getBoundaryLayerManifest()
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
