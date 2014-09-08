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

import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.VersionManifest;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class VersionManifestImpl implements VersionManifest, ModelXmlElement, ModelXmlHandler {

	private String formatId;
	private String versionString;

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		if(qName.equals(TAG_VERSION)) {
			String formatId = ModelXmlUtils.normalize(attributes, ATTR_VERSION_FORMAT);
			if(formatId!=null) {
				setFormatId(formatId);
			}

			return this;
		} else
			throw new SAXException("Unexpected opening tag "+qName+" in "+TAG_VERSION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		if(qName.equals(TAG_VERSION)) {
			setVersionString(text);

			return null;
		} else
			throw new SAXException("Unexpected closing tag "+qName+" in "+TAG_VERSION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		throw new SAXException("Unexpected nested element "+qName+" in "+TAG_VERSION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		ModelXmlUtils.writeVersionElement(serializer, this);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.VersionManifest#getFormatId()
	 */
	@Override
	public String getFormatId() {
		return formatId;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.VersionManifest#getVersionString()
	 */
	@Override
	public String getVersionString() {
		return versionString;
	}

	/**
	 * @param formatId the formatId to set
	 */
	public void setFormatId(String formatId) {
		this.formatId = formatId;
	}

	/**
	 * @param versionString the versionString to set
	 */
	public void setVersionString(String versionString) {
		if (versionString == null)
			throw new NullPointerException("Invalid versionString"); //$NON-NLS-1$

		this.versionString = versionString;
	}

}
