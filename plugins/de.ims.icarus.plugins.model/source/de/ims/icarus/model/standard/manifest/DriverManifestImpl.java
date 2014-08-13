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

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.Links.Link;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.classes.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DriverManifestImpl extends AbstractForeignImplementationManifest<DriverManifest> implements DriverManifest {

	private LocationType locationType;
	private final List<IndexManifest> indexManifests = new ArrayList<>();
	private final List<ModuleSpec> moduleSpecs = new ArrayList<>();
	private final List<ModuleManifest> moduleManifests = new ArrayList<>();
	private final ContextManifest contextManifest;

	public DriverManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, ContextManifest contextManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, contextManifest, ContextManifest.class);

		this.contextManifest = contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return super.isEmpty() && indexManifests.isEmpty() && moduleSpecs.isEmpty() && moduleManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		if(locationType!=null) {
			serializer.writeAttribute(ATTR_LOCATION_TYPE, locationType.getXmlValue());
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write module specs
		for(ModuleSpec moduleSpec : moduleSpecs) {
			ModelXmlUtils.writeModuleSpecElement(serializer, moduleSpec);
		}

		// Write module manifests
		for(ModuleManifest moduleManifest : moduleManifests) {
			moduleManifest.writeXml(serializer);
		}

		// Write index manifests
		for(IndexManifest indexManifest : indexManifests) {
			ModelXmlUtils.writeIndexElement(serializer, indexManifest);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String locationType = ModelXmlUtils.normalize(attributes, ATTR_LOCATION_TYPE);
		if(locationType!=null) {
			setLocationType(LocationType.parseLocationType(locationType));
		}
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_DRIVER: {
			readAttributes(attributes);
		} break;

		case TAG_INDEX: {
			return new IndexManifestImpl(this);
		}

		case TAG_MODULE_SPEC: {
			return new ModuleSpecImpl(this);
		}

		case TAG_MODULE: {
			return new ModuleManifestImpl(manifestLocation, getRegistry(), this);
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
		case TAG_DRIVER: {
			return null;
		}

		case TAG_MODULE_SPEC: {
			// no-op
		} break;

		case TAG_MODULE: {
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

		case TAG_INDEX: {
			addIndexManifest((IndexManifest) handler);
		} break;

		case TAG_MODULE_SPEC: {
			addModuleSpec((ModuleSpec) handler);
		} break;

		case TAG_MODULE: {
			addModuleManifest((ModuleManifest) handler);
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
		return TAG_DRIVER;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.DRIVER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getImplementationManifest()
	 */
	@Override
	public ImplementationManifest getImplementationManifest() {
		ImplementationManifest result = super.getImplementationManifest();
		if(result==null && hasTemplate()) {
			result = getTemplate().getImplementationManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getIndexManifests()
	 */
	@Override
	public List<IndexManifest> getIndexManifests() {
		List<IndexManifest> result = new ArrayList<>(indexManifests);

		if(hasTemplate()) {
			result.addAll(getTemplate().getIndexManifests());
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getLocationType()
	 */
	@Override
	public LocationType getLocationType() {
		LocationType result = locationType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getLocationType();
		}

		if(result==null)
			throw new ModelException(ModelError.MANIFEST_MISSING_LOCATION,
					"No location type available for driver manifest: "+getId()); //$NON-NLS-1$

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getModuleManifests()
	 */
	@Override
	public List<ModuleManifest> getModuleManifests() {
		List<ModuleManifest> result = new ArrayList<>(moduleManifests);

		if(hasTemplate()) {
			result.addAll(getTemplate().getModuleManifests());
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getModuleSpecs()
	 */
	@Override
	public List<ModuleSpec> getModuleSpecs() {
		List<ModuleSpec> result = new ArrayList<>(moduleSpecs);

		if(hasTemplate()) {
			result.addAll(getTemplate().getModuleSpecs());
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getModuleSpec(java.lang.String)
	 */
	@Override
	public ModuleSpec getModuleSpec(final String specId) {
		if (specId == null)
			throw new NullPointerException("Invalid specId"); //$NON-NLS-1$

		ModuleSpec result = null;

		for(ModuleSpec spec : moduleSpecs) {
			if(specId.equals(spec.getId())) {
				result = spec;
				break;
			}
		}

		if(result==null && hasTemplate()) {
			result = getTemplate().getModuleSpec(specId);
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getModuleManifest(java.lang.String)
	 */
	@Override
	public ModuleManifest getModuleManifest(String moduleId) {
		if (moduleId == null)
			throw new NullPointerException("Invalid specId"); //$NON-NLS-1$

		ModuleManifest result = null;

		for(ModuleManifest moduleManifest : moduleManifests) {
			if(moduleId.equals(moduleManifest.getId())) {
				result = moduleManifest;
				break;
			}
		}

		if(result==null && hasTemplate()) {
			result = getTemplate().getModuleManifest(moduleId);
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#addIndexManifest(de.ims.icarus.model.api.manifest.IndexManifest)
	 */
//	@Override
	public void addIndexManifest(IndexManifest indexManifest) {
		if (indexManifest == null)
			throw new NullPointerException("Invalid indexManifest");  //$NON-NLS-1$

		if(indexManifests.contains(indexManifest))
			throw new IllegalArgumentException("Duplicate index manifest: "+indexManifest); //$NON-NLS-1$

		indexManifests.add(indexManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#removeIndexManifest(de.ims.icarus.model.api.manifest.IndexManifest)
	 */
//	@Override
	public void removeIndexManifest(IndexManifest indexManifest) {
		if (indexManifest == null)
			throw new NullPointerException("Invalid indexManifest");  //$NON-NLS-1$

		if(!indexManifests.remove(indexManifest))
			throw new IllegalArgumentException("Unknown index manifest: "+indexManifest); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#addModuleManifest(de.ims.icarus.model.api.manifest.ModuleManifest)
	 */
//	@Override
	public void addModuleManifest(ModuleManifest moduleManifest) {
		if (moduleManifest == null)
			throw new NullPointerException("Invalid moduleManifest");  //$NON-NLS-1$

		if(moduleManifests.contains(moduleManifest))
			throw new IllegalArgumentException("Duplicate module manifest: "+moduleManifest); //$NON-NLS-1$

		moduleManifests.add(moduleManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#removeModuleManifest(de.ims.icarus.model.api.manifest.ModuleManifest)
	 */
//	@Override
	public void removeModuleManifest(ModuleManifest moduleManifest) {
		if (moduleManifest == null)
			throw new NullPointerException("Invalid moduleManifest");  //$NON-NLS-1$

		if(!moduleManifests.remove(moduleManifest))
			throw new IllegalArgumentException("Unknown module manifest: "+moduleManifest); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#addModuleManifest(de.ims.icarus.model.api.manifest.addModuleSpec)
	 */
//	@Override
	public void addModuleSpec(ModuleSpec moduleSpec) {
		if (moduleSpec == null)
			throw new NullPointerException("Invalid moduleSpec");  //$NON-NLS-1$

		if(moduleSpecs.contains(moduleSpec))
			throw new IllegalArgumentException("Duplicate module spec: "+moduleSpec); //$NON-NLS-1$

		moduleSpecs.add(moduleSpec);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#removeModuleManifest(de.ims.icarus.model.api.manifest.removeModuleSpec)
	 */
//	@Override
	public void removeModuleSpec(ModuleSpec moduleSpec) {
		if (moduleSpec == null)
			throw new NullPointerException("Invalid moduleSpec");  //$NON-NLS-1$

		if(!moduleSpecs.remove(moduleSpec))
			throw new IllegalArgumentException("Unknown module spec: "+moduleSpec); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#setLocationType(de.ims.icarus.model.io.LocationType)
	 */
//	@Override
	public void setLocationType(LocationType locationType) {
		if (locationType == null)
			throw new NullPointerException("Invalid locationType"); //$NON-NLS-1$

		this.locationType = locationType;
	}

	public static class ModuleSpecImpl extends DefaultModifiableIdentity implements ModuleSpec, ModelXmlElement, ModelXmlHandler {

		private final DriverManifest driverManifest;
		private boolean optional = DEFAULT_IS_OPTIONAL;
		private boolean customizable = DEFAULT_IS_CUSTOMIZABLE;
		private String extensionPointUid;

		public ModuleSpecImpl(DriverManifest driverManifest) {
			if (driverManifest == null)
				throw new NullPointerException("Invalid driverManifest");  //$NON-NLS-1$

			this.driverManifest = driverManifest;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity#hashCode()
		 */
		@Override
		public int hashCode() {
			int hash = driverManifest.hashCode()+1;
			if(getId()!=null) {
				hash *= getId().hashCode();
			}
			return hash;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof ModuleSpec) {
				ModuleSpec other = (ModuleSpec) obj;
				return ClassUtils.equals(getId(), other.getId());
			}
			return false;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity#toString()
		 */
		@Override
		public String toString() {
			return "ModuleSpec@"+ (getId()==null ? "<unnamed>" : getId()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/**
		 * @param attributes
		 */
		protected void readAttributes(Attributes attributes) {
			ModelXmlUtils.readIdentity(attributes, this);

			String optional = ModelXmlUtils.normalize(attributes, ATTR_OPTIONAL);
			if(optional!=null) {
				setOptional(Boolean.parseBoolean(optional));
			}

			String customizable = ModelXmlUtils.normalize(attributes, ATTR_CUSTOMIZABLE);
			if(customizable!=null) {
				setCustomizable(Boolean.parseBoolean(customizable));
			}
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ModelXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (qName) {
			case TAG_MODULE_SPEC: {
				readAttributes(attributes);
			} break;

			case TAG_EXTENSION_POINT: {
				// no-op
			} break;

			default:
				throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_MODULE_SPEC+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
		 */
		@Override
		public ModelXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
				throws SAXException {
			switch (qName) {
			case TAG_MODULE_SPEC: {
				return null;
			}

			case TAG_EXTENSION_POINT: {
				setExtensionPointUid(text);
			} break;

			default:
				throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_MODULE_SPEC+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			return this;
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				ModelXmlHandler handler) throws SAXException {
			throw new SAXException("Unexpected nested element "+qName+" in "+TAG_MODULE_SPEC+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
		 */
		@Override
		public void writeXml(XmlSerializer serializer) throws Exception {
			ModelXmlUtils.writeModuleSpecElement(serializer, this);
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec#getDriverManifest()
		 */
		@Override
		public DriverManifest getDriverManifest() {
			return driverManifest;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec#isOptional()
		 */
		@Override
		public boolean isOptional() {
			return optional;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec#isCustomizable()
		 */
		@Override
		public boolean isCustomizable() {
			return customizable;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec#getExtensionPointUid()
		 */
		@Override
		public String getExtensionPointUid() {
			return extensionPointUid;
		}

		/**
		 * @param optional the optional to set
		 */
		public void setOptional(boolean optional) {
			this.optional = optional;
		}

		/**
		 * @param customizable the customizable to set
		 */
		public void setCustomizable(boolean customizable) {
			this.customizable = customizable;
		}

		/**
		 * @param extensionPointUid the extensionPointUid to set
		 */
		public void setExtensionPointUid(String extensionPointUid) {
			this.extensionPointUid = extensionPointUid;
		}

	}

	public static class ModuleManifestImpl extends AbstractForeignImplementationManifest<ModuleManifestImpl> implements ModuleManifest {

		private final DriverManifest driverManifest;
		private ModuleSpecLink moduleSpec;

		/**
		 * @param manifestLocation
		 * @param registry
		 */
		public ModuleManifestImpl(ManifestLocation manifestLocation,
				CorpusRegistry registry, DriverManifest driverManifest) {
			super(manifestLocation, registry);

			verifyEnvironment(manifestLocation, driverManifest, DriverManifest.class);

			this.driverManifest = driverManifest;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
		 */
		@Override
		protected void readAttributes(Attributes attributes) {
			super.readAttributes(attributes);

			String moduleSpecId = ModelXmlUtils.normalize(attributes, ATTR_MODULE_SPEC_ID);
			if(moduleSpecId!=null) {
				setModuleSpecId(moduleSpecId);
			}
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
		 */
		@Override
		protected void writeAttributes(XmlSerializer serializer)
				throws Exception {
			super.writeAttributes(serializer);

			if(moduleSpec!=null) {
				serializer.writeAttribute(ATTR_MODULE_SPEC_ID, moduleSpec.getId());
			}
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.Manifest#getManifestType()
		 */
		@Override
		public ManifestType getManifestType() {
			return ManifestType.MODULE_MANIFEST;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
		 */
		@Override
		protected String xmlTag() {
			return TAG_MODULE;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractForeignImplementationManifest#getImplementationManifest()
		 */
		@Override
		public ImplementationManifest getImplementationManifest() {
			ImplementationManifest result = super.getImplementationManifest();
			if(result==null && hasTemplate()) {
				result = getTemplate().getImplementationManifest();
			}

			return result;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.DriverManifest.ModuleManifest#getDriverManifest()
		 */
		@Override
		public DriverManifest getDriverManifest() {
			return driverManifest;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.DriverManifest.ModuleManifest#getModuleSpecId()
		 */
		@Override
		public ModuleSpec getModuleSpec() {
			return moduleSpec==null ? null : moduleSpec.get();
		}

		/**
		 * @param moduleSpecId the moduleSpecId to set
		 */
		public void setModuleSpecId(String moduleSpecId) {
			moduleSpec = new ModuleSpecLink(moduleSpecId);
		}

		protected class ModuleSpecLink extends Link<ModuleSpec> {

			/**
			 * @param id
			 */
			public ModuleSpecLink(String id) {
				super(id);
			}

			/**
			 * @see de.ims.icarus.model.standard.manifest.Links.Link#resolve()
			 */
			@Override
			protected ModuleSpec resolve() {
				return getDriverManifest().getModuleSpec(getId());
			}

		}
	}
}
