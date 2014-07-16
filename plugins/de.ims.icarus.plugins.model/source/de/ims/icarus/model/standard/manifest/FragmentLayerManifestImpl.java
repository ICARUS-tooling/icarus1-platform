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

import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FragmentLayerManifestImpl extends MarkableLayerManifestImpl implements FragmentLayerManifest {

	private TargetLayerManifest valueManifest;
	private String annotationKey;
	private RasterizerManifest rasterizerManifest;

	/**
	 * @param manifestSource
	 * @param registry
	 * @param layerGroupManifest
	 */
	public FragmentLayerManifestImpl(ManifestSource manifestSource,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestSource, registry, layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractLayerManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		serializer.writeAttribute(ATTR_ANNOTATION_KEY, annotationKey);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		if(valueManifest!=null) {
			ModelXmlUtils.writeTargetLayerManifestElement(serializer, TAG_VALUE_LAYER, valueManifest);
		}

		if(rasterizerManifest!=null) {
			rasterizerManifest.writeXml(serializer);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_FRAGMENT_LAYER;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable#getTemplate()
	 */
	@Override
	public FragmentLayerManifest getTemplate() {
		return (FragmentLayerManifest) super.getTemplate();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.FragmentLayerManifest#getValueLayerManifest()
	 */
	@Override
	public TargetLayerManifest getValueLayerManifest() {
		return valueManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.FragmentLayerManifest#getAnnotationKey()
	 */
	@Override
	public String getAnnotationKey() {
		String result = annotationKey;
		if(result==null && hasTemplate()) {
			result = getTemplate().getAnnotationKey();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.FragmentLayerManifest#getRasterizerManifest()
	 */
	@Override
	public RasterizerManifest getRasterizerManifest() {
		RasterizerManifest result = rasterizerManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getRasterizerManifest();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.FragmentLayerManifest#setValueLayerManifest(de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest)
	 */
//	@Override
	public TargetLayerManifest setValueLayerId(String valueLayerId) {
		checkAllowsTargetLayer();
		TargetLayerManifest manifest = new TargetLayerManifestImpl(valueLayerId);
		valueManifest = manifest;
		return manifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.FragmentLayerManifest#setAnnotationKey(java.lang.String)
	 */
//	@Override
	public void setAnnotationKey(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		annotationKey = key;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.FragmentLayerManifest#setRasterizerManifest(de.ims.icarus.model.api.manifest.RasterizerManifest)
	 */
//	@Override
	public void setRasterizerManifest(RasterizerManifest rasterizerManifest) {
		if (rasterizerManifest == null)
			throw new NullPointerException("Invalid rasterizerManifest"); //$NON-NLS-1$

		this.rasterizerManifest = rasterizerManifest;
	}

}
