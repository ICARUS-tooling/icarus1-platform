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

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.VersionManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.Links.Link;
import de.ims.icarus.model.util.CorpusUtils;
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
public abstract class AbstractManifest<T extends Manifest> implements Manifest, ModelXmlElement, ModelXmlHandler {

	private TemplateLink<T> template;

	private String id;
	private boolean isTemplate;
	private VersionManifest versionManifest;

	private transient final ManifestLocation manifestLocation;
	private transient final CorpusRegistry registry;

	public static void verifyEnvironment(ManifestLocation manifestLocation, Object environment, Class<?> expected) {
		if(!manifestLocation.isTemplate() && environment==null)
			throw new ModelException(ModelError.MANIFEST_MISSING_ENVIRONMENT,
					"Missing environment of type "+expected.getName()); //$NON-NLS-1$
	}

	protected AbstractManifest(ManifestLocation manifestLocation, CorpusRegistry registry) {
		if (manifestLocation == null)
			throw new NullPointerException("Invalid manifestLocation");  //$NON-NLS-1$
		if (registry == null)
			throw new NullPointerException("Invalid registry");  //$NON-NLS-1$

		this.manifestLocation = manifestLocation;
		this.registry = registry;
		isTemplate = this.manifestLocation.isTemplate();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = getManifestType().hashCode()*manifestLocation.hashCode()*registry.hashCode();

		if(getId()!=null) {
			hash *= getId().hashCode();
		}

		return hash;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Manifest) {
			Manifest other = (Manifest) obj;

			//FIXME currently manifest location is excluded from equality check
			return getManifestType().equals(other.getManifestType())
//					&& manifestLocation.equals(other.getManifestLocation())
					&& registry.equals(other.getRegistry())
					&& ClassUtils.equals(getId(), other.getId());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String s = getManifestType().toString();

		if(getId()!=null) {
			s += "@"+getId(); //$NON-NLS-1$
		}

		return s;
	}

//	/**
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		int hash = getManifestType().hashCode()*manifestLocation.hashCode()*registry.hashCode();
//
//		if(id!=null) {
//			hash *= id.hashCode();
//		}
//
//		return hash;
//	}
//
//	/**
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if(obj instanceof Manifest) {
//			Manifest other = (Manifest) obj;
//			return getManifestType()==other.getManifestType()
//					&& registry==other.getRegistry()
//					&& manifestLocation.equals(other.getManifestLocation())
//					&& ClassUtils.equals(id, other.getId());
//		}
//		return false;
//	}

	protected void writeEmbedded(ModelXmlElement element, XmlSerializer serializer) throws Exception {
		if(element!=null) {
			element.writeXml(serializer);
			serializer.writeLineBreak();
		}
	}

	/**
	 * Check whether a flag is set and differs from a given default value. Only if both conditions are met
	 * will the flag be written to the provided serializer, using the specified name as identifier.
	 */
	protected void writeFlag(XmlSerializer serializer, String name, Boolean flag, boolean defaultValue) throws Exception {
		if(flag!=null && flag.booleanValue()!=defaultValue) {
			serializer.writeAttribute(name, flag.booleanValue());
		}
	}

	protected Boolean readFlag(Attributes attributes, String name) {
		String value = ModelXmlUtils.normalize(attributes, name);
		return value==null ? null : Boolean.valueOf(value);
	}

	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		serializer.writeAttribute(ATTR_ID, id);

		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());
		}
	}

	protected void readAttributes(Attributes attributes) {
		String id = ModelXmlUtils.normalize(attributes, ATTR_ID);
		if(id!=null) {
			setId(id);
		}

		String templateId = ModelXmlUtils.normalize(attributes, ATTR_TEMPLATE_ID);
		if(templateId!=null) {
			setTemplateId(templateId);
		}
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

		if(qName.equals(xmlTag())) {
			readAttributes(attributes);
			return this;
		} else if(qName.equals(TAG_VERSION)) {
			return new VersionManifestImpl();
		} else
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+xmlTag()+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {

		if(qName.equals(xmlTag())) {
			return null;
		} else
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+xmlTag()+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		if(qName.equals(TAG_VERSION)) {
			setVersionManifest((VersionManifest) handler);
		} else
			throw new SAXException("Unexpected nested element "+qName+" in "+xmlTag()+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void writeElements(XmlSerializer serializer) throws Exception {
		writeEmbedded(versionManifest, serializer);
	}

	protected abstract String xmlTag();

	protected abstract boolean isEmpty();

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		if(isEmpty()) {
			serializer.startEmptyElement(xmlTag());
		} else {
			serializer.startElement(xmlTag());
		}
		writeAttributes(serializer);
		writeElements(serializer);
		serializer.endElement(xmlTag());
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Manifest#getManifestLocation()
	 */
	@Override
	public ManifestLocation getManifestLocation() {
		return manifestLocation;
	}

	/**
	 * @return the registry
	 */
	@Override
	public CorpusRegistry getRegistry() {
		return registry;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		//FIXME do we really want to inherit id?
		String result = id;
		if(result==null && hasTemplate()) {
			result = getTemplate().getId();
		}
		return result;
	}

	/**
	 * @return the versionManifest
	 */
	@Override
	public VersionManifest getVersionManifest() {
		return versionManifest;
	}

	/**
	 * @param versionManifest the versionManifest to set
	 */
	public void setVersionManifest(VersionManifest versionManifest) {
		this.versionManifest = versionManifest;
	}

	/**
	 * @param id the id to set
	 */
//	@Override
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if(!CorpusUtils.isValidId(id))
			throw new IllegalArgumentException("Id format not supported: "+id); //$NON-NLS-1$

		this.id = id;
	}

//	/**
//	 * Applies the content of the given template to this derivable.
//	 * Note that a subclass should always invoke {@code super.readTemplate()} first!
//	 * @param template
//	 */
//	protected void readTemplate(T template) {
//		// for subclasses
//	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Manifest#setTemplateId(java.lang.String)
	 */
//	@Override
	public void setTemplateId(String templateId) {
		template = new TemplateLink<>(templateId);
	}

	@Override
	public T getTemplate() {
		return template==null ? null : template.get();
	}

	public boolean hasTemplate() {
		return template!=null;
	}

//	/**
//	 * Writes out the given {@code localValue} if it non-null and
//	 * does not equal the optional {@code templateValue}
//	 * @throws Exception
//	 */
//	protected void writeXmlAttribute(XmlSerializer serializer, String name,
//			String localValue, String templateValue) throws Exception {
//		if(localValue==null || (localValue!=null && localValue.equals(templateValue))) {
//			return;
//		}
//
//		serializer.writeAttribute(name, localValue);
//	}
//
//	protected void writeXmlAttribute(XmlSerializer serializer, String name,
//			boolean localValue, boolean templateValue) throws Exception {
//		if(localValue==templateValue) {
//			return;
//		}
//
//		serializer.writeAttribute(name, localValue);
//	}
//
//	protected void writeXmlAttribute(XmlSerializer serializer, String name,
//			int localValue, int templateValue) throws Exception {
//		if(localValue==templateValue) {
//			return;
//		}
//
//		serializer.writeAttribute(name, localValue);
//	}
//
//	protected void writeXmlAttribute(XmlSerializer serializer, String name,
//			Object localValue, Object templateValue) throws Exception {
//		if(localValue==null || (localValue!=null && localValue.equals(templateValue))) {
//			return;
//		}
//
//		if(localValue instanceof XmlResource) {
//			serializer.writeAttribute(name, ((XmlResource) localValue).getValue());
//		} else
//			LoggerFactory.warning(this, "Unable to serialize object to xml: "+localValue.getClass()); //$NON-NLS-1$
//	}
//
//	protected void writeXmlAttribute(XmlSerializer serializer, String name,
//			Object value) throws Exception {
//		if(value==null) {
//			return;
//		}
//
//		if(value instanceof XmlResource) {
//			serializer.writeAttribute(name, ((XmlResource) value).getValue());
//		} else
//			LoggerFactory.warning(this, "Unable to serialize object to xml: "+value.getClass()); //$NON-NLS-1$
//	}
//
//	/**
//	 * @throws Exception
//	 * @see de.ims.icarus.model.api.xml.XmlElement#writeXml(de.ims.icarus.model.api.xml.XmlSerializer)
//	 */
//	@Override
//	public void writeXml(XmlSerializer serializer) throws Exception {
//		serializer.startElement(getXmlTag());
//		if(hasTemplate()) {
//			writeTemplateXmlAttributes(serializer);
//			writeTemplateXmlElements(serializer);
//		} else {
//			writeFullXmlAttributes(serializer);
//			writeFullXmlElements(serializer);
//		}
//		serializer.endElement(getXmlTag());
//	}
//
//	protected void writeTemplateXmlAttributes(XmlSerializer serializer) throws Exception {
//		// no-op
//	}
//
//	protected void writeFullXmlAttributes(XmlSerializer serializer) throws Exception {
//		// no-op
//	}
//
//	protected void writeTemplateXmlElements(XmlSerializer serializer) throws Exception {
//		// no-op
//	}
//
//	protected void writeFullXmlElements(XmlSerializer serializer) throws Exception {
//		// no-op
//	}
//
//	protected abstract String getXmlTag();

	/**
	 * @see de.ims.icarus.model.api.manifest.Manifest#isTemplate()
	 */
	@Override
	public boolean isTemplate() {
		return isTemplate;
	}

	/**
	 * @param isTemplate the isTemplate to set
	 */
//	@Override
	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	protected class TemplateLink<D extends Manifest> extends Link<D> {

		/**
		 * @param abstractDerivable
		 * @param id
		 */
		public TemplateLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractManifest.Link#resolve()
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected D resolve() {
			return (D) registry.getTemplate(getId());
		}

	}

	protected class LayerTypeLink extends Link<LayerType> {

		/**
		 * @param id
		 */
		public LayerTypeLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.Links.Link#resolve()
		 */
		@Override
		protected LayerType resolve() {
			return registry.getLayerType(getId());
		}

	}
}
