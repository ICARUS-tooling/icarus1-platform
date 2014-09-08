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

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.Links.Link;
import de.ims.icarus.model.standard.manifest.Links.MemoryLink;
import de.ims.icarus.model.util.CorpusUtils;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextManifestImpl extends AbstractMemberManifest<ContextManifest> implements ContextManifest {

	// Lookup structures
//	private final List<LayerManifest> layerManifests = new ArrayList<>();
//	private final Map<String, LayerManifest> layerManifestLookup = new HashMap<>();

	// Main storage
	private final List<PrerequisiteManifest> prerequisiteManifests = new ArrayList<>();
	private final List<LayerGroupManifest> groupManifests = new ArrayList<>();

	private LayerLink primaryLayer;
	private LayerLink baseLayer;
	private LocationManifest locationManifest;

	private Boolean independent;
	private final CorpusManifest corpusManifest;
	private DriverManifest driverManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ContextManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry) {
		this(manifestLocation, registry, null);
	}

	public ContextManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, CorpusManifest corpusManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, corpusManifest, CorpusManifest.class);

		this.corpusManifest = corpusManifest;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return super.isEmpty() && prerequisiteManifests.isEmpty() && groupManifests.isEmpty() && driverManifest==null && locationManifest==null;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		// Write primary layer
		if(primaryLayer!=null) {
			serializer.writeAttribute(ATTR_PRIMARY_LAYER, primaryLayer.getId());
		}

		// Write base layer
		if(baseLayer!=null) {
			serializer.writeAttribute(ATTR_BASE_LAYER, baseLayer.getId());
		}

		// Write flags
		writeFlag(serializer, ATTR_INDEPENDENT, independent, DEFAULT_INDEPENDENT_VALUE);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write location manifest
		if(locationManifest!=null) {
			ModelXmlUtils.writeLocationElement(serializer, locationManifest);
			serializer.writeLineBreak();
		}

		// Write prerequisites
		if(!prerequisiteManifests.isEmpty()) {
			serializer.startElement(TAG_PREREQUISITES);

			for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
				ModelXmlUtils.writePrerequisiteElement(serializer, prerequisiteManifest);
			}

			serializer.endElement(TAG_PREREQUISITES);
			serializer.writeLineBreak();
		}

		// Write groups
		for(Iterator<LayerGroupManifest> it = groupManifests.iterator(); it.hasNext();) {
			it.next().writeXml(serializer);
			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		// Write driver
		writeEmbedded(driverManifest, serializer);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		// Read primary layer id
		String primaryLayerId = ModelXmlUtils.normalize(attributes, ATTR_PRIMARY_LAYER);
		if(primaryLayerId!=null) {
			setPrimaryLayerId(primaryLayerId);
		}

		// Read base layer id
		String baseLayerId = ModelXmlUtils.normalize(attributes, ATTR_BASE_LAYER);
		if(baseLayerId!=null) {
			setBaseLayerId(baseLayerId);
		}

		independent = readFlag(attributes, ATTR_INDEPENDENT);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_CONTEXT: {
			readAttributes(attributes);
		} break;

		case TAG_LOCATION: {
			return new LocationManifestImpl();
		}

		case TAG_PREREQUISITES: {
			// no-op
		} break;

		case TAG_PREREQUISITE: {
			String alias = ModelXmlUtils.normalize(attributes, ATTR_ALIAS);
			PrerequisiteManifestImpl prerequisite = addPrerequisite(alias);
			prerequisite.readAttributes(attributes);
		} break;

		case TAG_LAYER_GROUP: {
			return new LayerGroupManifestImpl(this);
		}

		case TAG_DRIVER: {
			return new DriverManifestImpl(manifestLocation, getRegistry(), this);
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
		case TAG_CONTEXT: {
			return null;
		}

		case TAG_PREREQUISITES: {
			// no-op
		} break;

		case TAG_PREREQUISITE: {
			// no-op
		} break;

		case TAG_DRIVER: {
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

		case TAG_LOCATION: {
			setLocationManifest((LocationManifest) handler);
		} break;

		case TAG_LAYER_GROUP : {
			addLayerGroup((LayerGroupManifest) handler);
		} break;

		case TAG_DRIVER: {
			setDriverManifest((DriverManifest) handler);
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
		return TAG_CONTEXT;
	}

//	private void resetLookup() {
//		layerManifests.clear();
//		layerManifestLookup.clear();
//	}
//
//	private void ensureLookup() {
//		if(layerManifests.isEmpty() && (!groupManifests.isEmpty() || !prerequisiteManifests.isEmpty())) {
//			for(LayerGroupManifest groupManifest : groupManifests) {
//				for(LayerManifest layerManifest : groupManifest.getLayerManifests()) {
//					layerManifests.add(layerManifest);
//					layerManifestLookup.put(layerManifest.getId(), layerManifest);
//				}
//			}
//
//
//		}
//	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getLayerManifests()
	 */
	@Override
	public List<LayerManifest> getLayerManifests() {
//		ensureLookup();
//		return CollectionUtils.getListProxy(layerManifests);

		List<LayerGroupManifest> groupManifests = getGroupManifests();

		// FIXME get a better default multiplier than 5 per group
		List<LayerManifest> result = new ArrayList<>(groupManifests.size()*5);

		for(LayerGroupManifest groupManifest : groupManifests) {
			result.addAll(groupManifest.getLayerManifests());
		}

		return result;
	}

//	private LayerManifest lookupLayer(String id, boolean localOnly) {
//		if (id == null)
//			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
//
//		ensureLookup();
//
//		LayerManifest result = layerManifestLookup.get(id);
//
//		if(result==null)
//			throw new ModelException(ModelError.MANIFEST_UNKNOWN_ID, "No layer available for id "+id+" in context "+getId()); //$NON-NLS-1$ //$NON-NLS-2$
//
//		if(localOnly && result.getContextManifest()!=this) {
//			result = null;
//		}
//
//		if(result==null)
//			throw new ModelException(ModelError.MANIFEST_UNKNOWN_ID, "No local layer available for id "+id+" in context "+getId()); //$NON-NLS-1$ //$NON-NLS-2$
//
//		return result;
//	}

	private LayerManifest lookupLayerManifest(String id) {

		LayerManifest result = null;

		List<LayerGroupManifest> groupManifests = getGroupManifests();

		for(int i=0; i<groupManifests.size(); i++) {
			result = groupManifests.get(i).getLayerManifest(id);
			if(result!=null) {
				break;
			}
		}

		/// TODO maybe throw exception when no corpus manifest is set here?
		if(result==null && corpusManifest!=null) {
			List<PrerequisiteManifest> prerequisiteManifests = getPrerequisites();

			for(int i=0; i<prerequisiteManifests.size(); i++) {
				PrerequisiteManifest prerequisiteManifest = prerequisiteManifests.get(i);
				if(id.equals(prerequisiteManifest.getAlias())) {
					CorpusManifest corpusManifest = getCorpusManifest();
					ContextManifest targetContext = corpusManifest.getContextManifest(prerequisiteManifest.getContextId());
					result = targetContext.getLayerManifest(prerequisiteManifest.getLayerId());
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getLayerManifest(java.lang.String)
	 */
	@Override
	public LayerManifest getLayerManifest(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$

//		ensureLookup();

//		LayerManifest result = layerManifestLookup.get(id);

		LayerManifest result = lookupLayerManifest(id);

		if(result==null)
			throw new ModelException(ModelError.MANIFEST_UNKNOWN_ID, "No layer available for id "+id+" in context "+getId()); //$NON-NLS-1$ //$NON-NLS-2$

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getPrerequisite(java.lang.String)
	 */
	@Override
	public PrerequisiteManifest getPrerequisite(String alias) {
		if (alias == null)
			throw new NullPointerException("Invalid alias"); //$NON-NLS-1$

		for(PrerequisiteManifest prerequisiteManifest : prerequisiteManifests) {
			if(alias.equals(prerequisiteManifest.getAlias())) {
				return prerequisiteManifest;
			}
		}

		return hasTemplate() ? getTemplate().getPrerequisite(alias) : null;
	}

//	/**
//	 * @see de.ims.icarus.model.api.manifest.ContextManifest#setName(java.lang.String)
//	 */
//	@Override
//	public void setName(String newName) {
//		throw new UnsupportedOperationException("Renaming not supported"); //$NON-NLS-1$
//	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getCorpusManifest()
	 */
	@Override
	public CorpusManifest getCorpusManifest() {
		return corpusManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getLocationManifest()
	 */
	@Override
	public LocationManifest getLocationManifest() {
		return locationManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#setLocationManifest(de.ims.icarus.model.api.manifest.LocationManifest)
	 */
//	@Override
	public void setLocationManifest(LocationManifest manifest) {
		if(isTemplate())
			throw new IllegalStateException("Cannot assign location manifest to template"); //$NON-NLS-1$

		this.locationManifest = manifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#isIndependentContext()
	 */
	@Override
	public boolean isIndependentContext() {
		if(independent==null) {
			return hasTemplate() ? getTemplate().isIndependentContext() : DEFAULT_INDEPENDENT_VALUE;
		} else {
			return independent.booleanValue();
		}
	}

	/**
	 * @param independent the independent to set
	 */
//	@Override
	public void setIndependentContext(boolean independent) {
		this.independent = independent;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#isRootContext()
	 */
	@Override
	public boolean isRootContext() {
		return corpusManifest!=null && corpusManifest.getRootContextManifest()==this;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTEXT_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getDriverManifest()
	 */
	@Override
	public DriverManifest getDriverManifest() {
		DriverManifest result = driverManifest;
		if(result==null && hasTemplate()) {
			result = getTemplate().getDriverManifest();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getPrerequisites()
	 */
	@Override
	public List<PrerequisiteManifest> getPrerequisites() {
		List<PrerequisiteManifest> result = new ArrayList<>(prerequisiteManifests);

		if(isTemplate() && hasTemplate()) {
			result.addAll(getTemplate().getPrerequisites());
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getGroupManifests()
	 */
	@Override
	public List<LayerGroupManifest> getGroupManifests() {
		List<LayerGroupManifest> result = new ArrayList<>(groupManifests);

		if(hasTemplate()) {
			result.addAll(getTemplate().getGroupManifests());
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ContextManifest#getPrimaryLayerManifest()
	 */
	@Override
	public MarkableLayerManifest getPrimaryLayerManifest() {
		if(primaryLayer!=null) {
			return primaryLayer.get();
		}

		if(hasTemplate()) {
			return getTemplate().getPrimaryLayerManifest();
		}

		throw new IllegalStateException("No primary layer defined for context: "+this); //$NON-NLS-1$
	}

	/**
	 * @param primaryLayerManifest the primaryLayerManifest to set
	 */
//	@Override
	public void setPrimaryLayerId(String primaryLayerId) {
		if (primaryLayerId == null)
			throw new NullPointerException("Invalid primaryLayerId"); //$NON-NLS-1$

		primaryLayer = new LayerLink(primaryLayerId);
	}

	/**
	 * @return the baseLayerManifest
	 */
	@Override
	public MarkableLayerManifest getBaseLayerManifest() {
		if(baseLayer!=null) {
			return baseLayer.get();
		}

		if(hasTemplate()) {
			return getTemplate().getBaseLayerManifest();
		}

		return null;
	}

	/**
	 * @param baseLayerManifest the baseLayerManifest to set
	 */
//	@Override
	public void setBaseLayerId(String baseLayerId) {
		if (baseLayerId == null)
			throw new NullPointerException("Invalid baseLayerId"); //$NON-NLS-1$

		baseLayer = new LayerLink(baseLayerId);
	}

	/**
	 * @param driverManifest the driverManifest to set
	 */
//	@Override
	public void setDriverManifest(DriverManifest driverManifest) {
		if (driverManifest == null)
			throw new NullPointerException("Invalid driverManifest"); //$NON-NLS-1$

		this.driverManifest = driverManifest;
	}

//	@Override
	public PrerequisiteManifestImpl addPrerequisite(String alias) {
		if (alias == null)
			throw new NullPointerException("Invalid alias");  //$NON-NLS-1$

		for(int i=0; i<prerequisiteManifests.size(); i++) {
			if(alias.equals(prerequisiteManifests.get(i).getAlias()))
				throw new IllegalArgumentException("Duplicate prerequisite alias: "+alias); //$NON-NLS-1$
		}

		PrerequisiteManifestImpl result = new PrerequisiteManifestImpl(alias);
		prerequisiteManifests.add(result);

		return result;
	}

//	@Override
	public void addLayerGroup(LayerGroupManifest groupManifest) {
		if (groupManifest == null)
			throw new NullPointerException("Invalid groupManifest"); //$NON-NLS-1$

		if(groupManifest.getContextManifest()!=this)
			throw new IllegalArgumentException("Layer group already hosted in foreign context: "+groupManifest); //$NON-NLS-1$

		if(groupManifests.contains(groupManifest))
			throw new IllegalArgumentException("Layer group already present: "+groupManifest); //$NON-NLS-1$

		groupManifests.add(groupManifest);

//		resetLookup();
	}

//	@Override
	public void removeLayerGroup(LayerGroupManifest groupManifest) {
		if (groupManifest == null)
			throw new NullPointerException("Invalid groupManifest"); //$NON-NLS-1$

		if(!groupManifests.remove(groupManifest))
			throw new IllegalArgumentException("Layer group not present: "+groupManifest); //$NON-NLS-1$

//		resetLookup();
	}

	protected class LayerLink extends Link<MarkableLayerManifest> {

		/**
		 * @param id
		 */
		public LayerLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.Links.Link#resolve()
		 */
		@Override
		protected MarkableLayerManifest resolve() {
			return (MarkableLayerManifest) getLayerManifest(getId());
		}

	}

	protected class PrerequisiteLink extends MemoryLink<PrerequisiteManifest> {

		/**
		 * @param id
		 */
		public PrerequisiteLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.Links.Link#resolve()
		 */
		@Override
		protected PrerequisiteManifest resolve() {
			ContextManifest contextManifest = ContextManifestImpl.this.getTemplate();
			return contextManifest==null ? null : contextManifest.getPrerequisite(getId());
		}

	}

	public class PrerequisiteManifestImpl implements PrerequisiteManifest {

		private final String alias;
		private final PrerequisiteLink unresolvedForm;

		private String layerId;
		private String typeId;
		private String contextId;
		private String description;

		PrerequisiteManifestImpl(String alias) {
			if (alias == null)
				throw new NullPointerException("Invalid alias");  //$NON-NLS-1$

			if(!CorpusUtils.isValidId(alias))
				throw new IllegalArgumentException("Alias format not supported: "+alias); //$NON-NLS-1$

			this.alias = alias;
			this.unresolvedForm = new PrerequisiteLink(alias);
		}

		protected void readAttributes(Attributes attributes) {
			layerId = ModelXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			typeId = ModelXmlUtils.normalize(attributes, ATTR_TYPE_ID);
			contextId = ModelXmlUtils.normalize(attributes, ATTR_CONTEXT_ID);
			description = ModelXmlUtils.normalize(attributes, ATTR_DESCRIPTION);
		}

		/**
		 * @return the description
		 */
		@Override
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getContextManifest()
		 */
		@Override
		public ContextManifest getContextManifest() {
			return ContextManifestImpl.this;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getContextId()
		 */
		@Override
		public String getContextId() {
			return contextId;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getTypeId()
		 */
		@Override
		public String getTypeId() {
			return typeId;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest#getAlias()
		 */
		@Override
		public String getAlias() {
			return alias;
		}

		/**
		 * @return the unresolvedForm
		 */
		@Override
		public PrerequisiteManifest getUnresolvedForm() {
			return unresolvedForm.get();
		}

		/**
		 * @param layerId the layerId to set
		 */
		public void setLayerId(String layerId) {
			this.layerId = layerId;
		}

		/**
		 * @param typeId the typeId to set
		 */
		public void setTypeId(String typeId) {
			this.typeId = typeId;
		}

		/**
		 * @param contextId the contextId to set
		 */
		public void setContextId(String contextId) {
			this.contextId = contextId;
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return alias.hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof PrerequisiteManifest) {
				return alias.equals(((PrerequisiteManifest)obj).getAlias());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Prerequisite:"+alias; //$NON-NLS-1$
		}

	}
}
