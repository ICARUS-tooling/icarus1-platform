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

import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ImplementationManifestImpl extends AbstractMemberManifest<ImplementationManifest> implements ImplementationManifest {

	private SourceType sourceType;
	private String source;
	private String classname;
	private Boolean useFactory;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	public ImplementationManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry) {
		super(manifestLocation, registry);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Manifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.IMPLEMENTATION_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		// Write source
		serializer.writeAttribute(ATTR_SOURCE, source);

		// Write classname
		serializer.writeAttribute(ATTR_CLASSNAME, classname);

		// Write source type
		if(sourceType!=null && sourceType!=SourceType.DEFAULT) {
			serializer.writeAttribute(ATTR_SOURCE_TYPE, sourceType.getXmlValue());
		}

		// Write flags
		writeFlag(serializer, ATTR_FACTORY, useFactory, DEFAULT_USE_FACTORY_VALUE);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		setSource(ModelXmlUtils.normalize(attributes, ATTR_SOURCE));
		setClassname(ModelXmlUtils.normalize(attributes, ATTR_CLASSNAME));

		String type = ModelXmlUtils.normalize(attributes, ATTR_SOURCE_TYPE);
		if(type!=null) {
			this.sourceType = SourceType.parseSourceType(type);
		}

		useFactory = readFlag(attributes, ATTR_FACTORY);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_IMPLEMENTATION: {
			readAttributes(attributes);
		} break;

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
		case TAG_IMPLEMENTATION: {
			return null;
		}

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}
	}


	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_IMPLEMENTATION;
	}


	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#getSourceType()
	 */
	@Override
	public SourceType getSourceType() {
		SourceType result = sourceType;
		if(result==null && hasTemplate()) {
			result = getTemplate().getSourceType();
		}
		if(result==null) {
			result = DEFAULT_SOURCE_TYPE;
		}
		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#getSource()
	 */
	@Override
	public String getSource() {
		String result = source;
		if(result==null && hasTemplate()) {
			result = getTemplate().getSource();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#getClassname()
	 */
	@Override
	public String getClassname() {
		String result = classname;
		if(result==null && hasTemplate()) {
			result = getTemplate().getClassname();
		}
		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.ImplementationManifest#isUseFactory()
	 */
	@Override
	public boolean isUseFactory() {
		if(useFactory==null) {
			return hasTemplate() ? getTemplate().isUseFactory() : DEFAULT_USE_FACTORY_VALUE;
		} else {
			return useFactory.booleanValue();
		}
	}

	/**
	 * @param sourceType the sourceType to set
	 */
//	@Override
	public void setSourceType(SourceType sourceType) {
		if (sourceType == null)
			throw new NullPointerException("Invalid sourceType"); //$NON-NLS-1$

		this.sourceType = sourceType;
	}

	/**
	 * @param source the source to set
	 */
//	@Override
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @param classname the classname to set
	 */
//	@Override
	public void setClassname(String classname) {
		this.classname = classname;
	}

	/**
	 * @param useFactory the useFactory to set
	 */
//	@Override
	public void setUseFactory(boolean useFactory) {
		this.useFactory = useFactory;
	}
}
