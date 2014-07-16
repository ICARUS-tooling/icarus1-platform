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

import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.Derivable;
import de.ims.icarus.model.api.manifest.ManifestSource;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.XmlSerializer;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractDerivable<T extends Derivable> extends LazyResolver implements Derivable, ModelXmlElement {

	private TemplateLink<T> template;

	private String id;
	private boolean isTemplate = false;

	private final ManifestSource manifestSource;
	private final CorpusRegistry registry;

	protected AbstractDerivable(ManifestSource manifestSource, CorpusRegistry registry) {
		if (manifestSource == null)
			throw new NullPointerException("Invalid manifestSource");  //$NON-NLS-1$
		if (registry == null)
			throw new NullPointerException("Invalid registry");  //$NON-NLS-1$

		this.manifestSource = manifestSource;
		this.registry = registry;
	}

	protected void writeEmbedded(ModelXmlElement element, XmlSerializer serializer) throws Exception {
		if(element!=null) {
			element.writeXml(serializer);
		}
	}

	protected void writeFlag(XmlSerializer serializer, String name, Boolean flag, boolean defaultValue) throws Exception {
		if(flag!=null && flag.booleanValue()!=defaultValue) {
			serializer.writeAttribute(name, flag.booleanValue());
		}
	}

	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		serializer.writeAttribute(ATTR_ID, id);

		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());
		}
	}

	protected void writeElements(XmlSerializer serializer) throws Exception {
		// for subclasses
	}

	protected abstract String xmlTag();

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		serializer.startElement(xmlTag());
		writeAttributes(serializer);
		writeElements(serializer);
		serializer.endElement(xmlTag());
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Manifest#getManifestSource()
	 */
	@Override
	public ManifestSource getManifestSource() {
		return manifestSource;
	}

	/**
	 * @return the registry
	 */
	public CorpusRegistry getRegistry() {
		return registry;
	}

	/**
	 * @return the id
	 */
	@Override
	public String getId() {
		String result = id;
		if(result==null && hasTemplate()) {
			result = getTemplate().getId();
		}
		return result;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(String id) {
		if (id == null)
			throw new NullPointerException("Invalid id"); //$NON-NLS-1$
		if(!CorpusRegistry.isValidId(id))
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
	 * @see de.ims.icarus.model.api.manifest.Derivable#setTemplateId(java.lang.String)
	 */
	@Override
	public void setTemplateId(String templateId) {
		template = new TemplateLink<>(templateId);
	}

	@Override
	public T getTemplate() {
		return template.get();
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
	 * @see de.ims.icarus.model.api.manifest.Derivable#isTemplate()
	 */
	@Override
	public boolean isTemplate() {
		return isTemplate;
	}

	/**
	 * @param isTemplate the isTemplate to set
	 */
	@Override
	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String id = getId();
		return id==null ? super.toString() : getClass().getName()+"@"+id; //$NON-NLS-1$
	}

	protected class TemplateLink<D extends Derivable> extends Link<D> {

		/**
		 * @param abstractDerivable
		 * @param id
		 */
		public TemplateLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractDerivable.Link#resolve()
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
		 * @see de.ims.icarus.model.standard.manifest.LazyResolver.Link#resolve()
		 */
		@Override
		protected LayerType resolve() {
			return registry.getLayerType(getId());
		}

	}
}
