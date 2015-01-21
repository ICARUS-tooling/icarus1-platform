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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FragmentLayerManifestImpl extends ItemLayerManifestImpl implements FragmentLayerManifest {

	private TargetLayerManifest valueManifest;
	private String annotationKey;
	private RasterizerManifest rasterizerManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 * @param layerGroupManifest
	 */
	public FragmentLayerManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry, layerGroupManifest);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return super.isEmpty() && rasterizerManifest==null && valueManifest==null;
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
	 * @see de.ims.icarus.model.standard.manifest.ItemLayerManifestImpl#writeElements(de.ims.icarus.model.xml.XmlSerializer)
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
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String annotationKey = ModelXmlUtils.normalize(attributes, ATTR_ANNOTATION_KEY);
		if(annotationKey!=null) {
			setAnnotationKey(annotationKey);
		}
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_FRAGMENT_LAYER: {
			readAttributes(attributes);
		} break;

		case TAG_VALUE_LAYER: {
			String valueLayerId = ModelXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			setValueLayerId(valueLayerId);
		} break;

		case TAG_RASTERIZER: {
			return new RasterizerManifestImpl(manifestLocation, getRegistry());
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
		case TAG_FRAGMENT_LAYER: {
			return null;
		}

		case TAG_VALUE_LAYER: {
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

		case TAG_RASTERIZER: {
			setRasterizerManifest((RasterizerManifest) handler);
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.ItemLayerManifestImpl#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_FRAGMENT_LAYER;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.ItemLayerManifestImpl#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#getTemplate()
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
