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
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.HighlightLayerManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LayerGroupManifestImpl extends LazyResolver implements LayerGroupManifest, ModelXmlHandler {

	private ContextManifest contextManifest;

	private final List<LayerManifest> layerManifests = new ArrayList<>();
	private LayerLink primaryLayer;
	private boolean independent = DEFAULT_INDEPENDENT_VALUE;
	private String name;

	public LayerGroupManifestImpl(ContextManifest contextManifest) {
		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$

		this.contextManifest = contextManifest;
	}

	public LayerGroupManifestImpl(ContextManifest contextManifest, String name) {
		if (contextManifest == null)
			throw new NullPointerException("Invalid contextManifest");  //$NON-NLS-1$
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.contextManifest = contextManifest;
		this.name = name;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		serializer.startElement(TAG_LAYER_GROUP);

		serializer.writeAttribute(ATTR_ID, name);
		if(independent!=DEFAULT_INDEPENDENT_VALUE) {
			serializer.writeAttribute(ATTR_INDEPENDENT, independent);
		}
		serializer.writeAttribute(ATTR_PRIMARY_LAYER, primaryLayer.getId());

		for(Iterator<LayerManifest> it = layerManifests.iterator(); it.hasNext();) {
			it.next().writeXml(serializer);
			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		serializer.endElement(TAG_LAYER_GROUP);
	}

	private CorpusRegistry getRegistry() {
		if(contextManifest==null)
			throw new IllegalStateException("Context manifest required for registry lookup"); //$NON-NLS-1$

		return contextManifest.getRegistry();
	}

	protected void readAttributes(Attributes attributes) {
		setName(ModelXmlUtils.normalize(attributes, ATTR_ID));

		String independent = ModelXmlUtils.normalize(attributes, ATTR_INDEPENDENT);
		if(independent!=null) {
			this.independent = Boolean.parseBoolean(independent);
		} else {
			this.independent = DEFAULT_INDEPENDENT_VALUE;
		}

		String primaryLayerId = ModelXmlUtils.normalize(attributes, ATTR_PRIMARY_LAYER);
		setPrimaryLayerId(primaryLayerId);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_LAYER_GROUP: {
			readAttributes(attributes);
		} break;

		case TAG_MARKABLE_LAYER : {
			return new MarkableLayerManifestImpl(manifestLocation, getRegistry(), this);
		}

		case TAG_STRUCTURE_LAYER : {
			return new StructureLayerManifestImpl(manifestLocation, getRegistry(), this);
		}

		case TAG_ANNOTATION_LAYER : {
			return new AnnotationLayerManifestImpl(manifestLocation, getRegistry(), this);
		}

		case TAG_FRAGMENT_LAYER : {
			return new FragmentLayerManifestImpl(manifestLocation, getRegistry(), this);
		}

		case TAG_HIGHLIGHT_LAYER : {
			return new HighlightLayerManifestImpl(manifestLocation, getRegistry(), this);
		}

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_LAYER_GROUP+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_LAYER_GROUP: {
			return null;
		}

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_LAYER_GROUP+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_MARKABLE_LAYER : {
			addLayerManifest((MarkableLayerManifest) handler);
		} break;

		case TAG_STRUCTURE_LAYER : {
			addLayerManifest((StructureLayerManifest) handler);
		} break;

		case TAG_ANNOTATION_LAYER : {
			addLayerManifest((AnnotationLayerManifest) handler);
		} break;

		case TAG_FRAGMENT_LAYER : {
			addLayerManifest((FragmentLayerManifest) handler);
		} break;

		case TAG_HIGHLIGHT_LAYER : {
			addLayerManifest((HighlightLayerManifest) handler);
		} break;

		default:
			break;
		}
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
		return CollectionUtils.getListProxy(layerManifests);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getPrimaryLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getPrimaryLayerManifest() {
		return primaryLayer.get();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#isIndependent()
	 */
	@Override
	public boolean isIndependent() {
		return independent;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null)
			throw new NullPointerException("Invalid name"); //$NON-NLS-1$

		this.name = name;
	}

	/**
	 * @param independent the independent to set
	 */
//	@Override
	public void setIndependent(boolean independent) {
		this.independent = independent;
	}

//	@Override
	public void addLayerManifest(LayerManifest layerManifest) {
		if (layerManifest == null)
			throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$

		if(!layerManifests.add(layerManifest))
			throw new IllegalArgumentException("Layer manifest already present in group: "+layerManifest.getId()); //$NON-NLS-1$
	}

//	@Override
	public void removeLayerManifest(LayerManifest layerManifest) {
		if (layerManifest == null)
			throw new NullPointerException("Invalid layerManifest"); //$NON-NLS-1$

		if(!layerManifests.remove(layerManifest))
			throw new IllegalArgumentException("Layer manifest not present in group: "+layerManifest.getId()); //$NON-NLS-1$
	}

	/**
	 * @param primaryLayerManifest the primaryLayerManifest to set
	 */
//	@Override
	public void setPrimaryLayerId(String primaryLayerId) {
//		if (primaryLayerManifest == null)
//			throw new NullPointerException("Invalid primaryLayerManifest"); //$NON-NLS-1$
//		if(!layerManifests.contains(primaryLayerManifest))
//			throw new IllegalArgumentException("Primary layer manifest not added as contained layer: "+primaryLayerManifest.getId()); //$NON-NLS-1$
//
//		this.primaryLayerManifest = primaryLayerManifest;

		primaryLayer = new LayerLink(primaryLayerId);
	}

//	/**
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		return name.hashCode();
//	}
//
//	/**
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if(obj instanceof LayerGroupManifest) {
//			return name.equals(((LayerGroupManifest)obj).getName());
//		}
//
//		return false;
//	}

//	/**
//	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#setContextManifest(de.ims.icarus.model.api.manifest.ContextManifest)
//	 */
//	@Override
//	public void setContextManifest(ContextManifest contextManifest) {
//		if (contextManifest == null)
//			throw new NullPointerException("Invalid contextManifest"); //$NON-NLS-1$
//
//		this.contextManifest = contextManifest;
//	}

//	/**
//	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#setName(java.lang.String)
//	 */
//	@Override
//	public void setName(String name) {
//		if (name == null)
//			throw new NullPointerException("Invalid name"); //$NON-NLS-1$
//
//		this.name = name;
//	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LayerGroup:"+name; //$NON-NLS-1$
	}

	private LayerManifest lookupLayer(String id, boolean localOnly) {
		LayerManifest result = null;

		for(LayerManifest layerManifest : layerManifests) {
			if(id.equals(layerManifest.getId())) {
				result = layerManifest;
				break;
			}
		}

		if(result==null && !localOnly && contextManifest!=null) {
			result = contextManifest.getLayerManifest(id);
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerGroupManifest#getLayerManifest(java.lang.String)
	 */
	@Override
	public LayerManifest getLayerManifest(String id) {
		return lookupLayer(id, true);
	}

	protected class LayerLink extends Link<MarkableLayerManifest> {

		/**
		 * @param lazyResolver
		 * @param id
		 */
		public LayerLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.LazyResolver.Link#resolve()
		 */
		@Override
		protected MarkableLayerManifest resolve() {
			return (MarkableLayerManifest) lookupLayer(getId(), true);
		}

	}
}
