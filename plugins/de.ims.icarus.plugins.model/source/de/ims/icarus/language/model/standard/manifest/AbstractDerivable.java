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
package de.ims.icarus.language.model.standard.manifest;

import de.ims.icarus.language.model.api.manifest.Derivable;
import de.ims.icarus.language.model.xml.XmlElement;
import de.ims.icarus.language.model.xml.XmlResource;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.logging.LoggerFactory;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractDerivable<T extends Derivable> implements XmlElement, Derivable {

	private T template;

	private boolean isTemplate = false;

	public AbstractDerivable() {
		template = null;
	}

	/**
	 * @see de.ims.icarus.language.model.api.manifest.Derivable#setTemplate(de.ims.icarus.language.model.api.manifest.Derivable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setTemplate(Derivable template) {
		if (template == null)
			throw new NullPointerException("Invalid template"); //$NON-NLS-1$

		this.template = (T) template;
		readTemplate((T) template);
	}

	protected void readTemplate(T template) {
		// for subclasses
	}

	@Override
	public T getTemplate() {
		return template;
	}

	public boolean hasTemplate() {
		return template!=null;
	}

	/**
	 * Writes out the given {@code localValue} if it non-null and
	 * does not equal the optional {@code templateValue}
	 * @throws Exception
	 */
	protected void writeXmlAttribute(XmlSerializer serializer, String name,
			String localValue, String templateValue) throws Exception {
		if(localValue==null || (localValue!=null && localValue.equals(templateValue))) {
			return;
		}

		serializer.writeAttribute(name, localValue);
	}

	protected void writeXmlAttribute(XmlSerializer serializer, String name,
			boolean localValue, boolean templateValue) throws Exception {
		if(localValue==templateValue) {
			return;
		}

		serializer.writeAttribute(name, localValue);
	}

	protected void writeXmlAttribute(XmlSerializer serializer, String name,
			int localValue, int templateValue) throws Exception {
		if(localValue==templateValue) {
			return;
		}

		serializer.writeAttribute(name, localValue);
	}

	protected void writeXmlAttribute(XmlSerializer serializer, String name,
			Object localValue, Object templateValue) throws Exception {
		if(localValue==null || (localValue!=null && localValue.equals(templateValue))) {
			return;
		}

		if(localValue instanceof XmlResource) {
			serializer.writeAttribute(name, ((XmlResource) localValue).getValue());
		} else
			LoggerFactory.warning(this, "Unable to serialize object to xml: "+localValue.getClass()); //$NON-NLS-1$
	}

	protected void writeXmlAttribute(XmlSerializer serializer, String name,
			Object value) throws Exception {
		if(value==null) {
			return;
		}

		if(value instanceof XmlResource) {
			serializer.writeAttribute(name, ((XmlResource) value).getValue());
		} else
			LoggerFactory.warning(this, "Unable to serialize object to xml: "+value.getClass()); //$NON-NLS-1$
	}

	/**
	 * @throws Exception
	 * @see de.ims.icarus.language.model.api.xml.XmlElement#writeXml(de.ims.icarus.language.model.api.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		serializer.startElement(getXmlTag());
		if(hasTemplate()) {
			writeTemplateXmlAttributes(serializer);
			writeTemplateXmlElements(serializer);
		} else {
			writeFullXmlAttributes(serializer);
			writeFullXmlElements(serializer);
		}
		serializer.endElement(getXmlTag());
	}

	protected void writeTemplateXmlAttributes(XmlSerializer serializer) throws Exception {
		// no-op
	}

	protected void writeFullXmlAttributes(XmlSerializer serializer) throws Exception {
		// no-op
	}

	protected void writeTemplateXmlElements(XmlSerializer serializer) throws Exception {
		// no-op
	}

	protected void writeFullXmlElements(XmlSerializer serializer) throws Exception {
		// no-op
	}

	protected abstract String getXmlTag();

	/**
	 * @see de.ims.icarus.language.model.api.manifest.Derivable#isTemplate()
	 */
	@Override
	public boolean isTemplate() {
		return isTemplate;
	}

	/**
	 * @param isTemplate the isTemplate to set
	 */
	public void setTemplate(boolean isTemplate) {
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
}
