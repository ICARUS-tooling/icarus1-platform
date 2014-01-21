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

import java.io.IOException;

import de.ims.icarus.language.model.xml.XmlSerializer;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class DerivedObject<T extends Object> {

	private String templateId;
	private T template;

	public void setTemplateId(String templateId) {
		if (templateId == null)
			throw new NullPointerException("Invalid templateId"); //$NON-NLS-1$
		if(this.templateId!=null)
			throw new IllegalStateException("Template id already set"); //$NON-NLS-1$

		this.templateId = templateId;
	}

	public synchronized T getTemplate() {
		if(template==null) {
			if(templateId==null)
				throw new IllegalStateException("Missing template id"); //$NON-NLS-1$

			template = resolveTemplate(templateId);
			templateLoaded(template);
		}

		return template;
	}

	protected void templateLoaded(T template) {
		// for subclasses
	}

	/**
	 * If a template-id has been set, this method makes sure the template
	 * is actually loaded.
	 */
	protected void checkTemplate() {
		if(hasTemplate()) {
			getTemplate();
		}
	}

	protected abstract T resolveTemplate(String templateId);

	public boolean hasTemplate() {
		return templateId!=null;
	}

	/**
	 * Writes the template-id as an attribute (if present)
	 *
	 * @param serializer
	 * @throws IOException
	 */
	protected void defaultWriteXml(XmlSerializer serializer) throws IOException {
		if(hasTemplate()) {
			serializer.writeAttribute("template-id", templateId); //$NON-NLS-1$
		}
	}
}
