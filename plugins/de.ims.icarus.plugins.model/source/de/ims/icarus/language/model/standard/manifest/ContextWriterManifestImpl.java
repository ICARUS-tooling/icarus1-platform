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

import de.ims.icarus.language.model.manifest.ContextWriterManifest;
import de.ims.icarus.language.model.manifest.ManifestType;
import de.ims.icarus.language.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextWriterManifestImpl extends AbstractManifest<ContextWriterManifest> implements ContextWriterManifest {

	private String formatId;


	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#readTemplate(de.ims.icarus.language.model.manifest.MemberManifest)
	 */
	@Override
	protected void readTemplate(ContextWriterManifest template) {
		super.readTemplate(template);

		if(formatId==null) {
			formatId = template.getFormatId();
		}
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.CONTEXT_WRITER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractDerivable#getXmlTag()
	 */
	@Override
	protected String getXmlTag() {
		return "context-writer"; //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.manifest.ContextReaderManifest#getFormatId()
	 */
	@Override
	public String getFormatId() {
		return formatId;
	}

	/**
	 * @param formatId the formatId to set
	 */
	public void setFormatId(String formatId) {
		if (formatId == null)
			throw new NullPointerException("Invalid formatId"); //$NON-NLS-1$

		this.formatId = formatId;
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeTemplateXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeTemplateXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeTemplateXmlAttributes(serializer);

		writeXmlAttribute(serializer, "format", formatId, getTemplate().getFormatId()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.language.model.standard.manifest.AbstractManifest#writeFullXmlAttributes(de.ims.icarus.language.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeFullXmlAttributes(XmlSerializer serializer)
			throws Exception {
		super.writeFullXmlAttributes(serializer);

		serializer.writeAttribute("format", formatId); //$NON-NLS-1$
	}

}
