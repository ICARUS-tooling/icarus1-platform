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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.util.types.Url;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.classes.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DocumentationImpl extends DefaultModifiableIdentity implements Documentation {

	private String content;

	private final List<Resource> resources = new ArrayList<>();

	public DocumentationImpl() {
		// no-op
	}

	public DocumentationImpl(String content) {
		setContent(content);
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 1;

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
		if(obj instanceof Documentation) {
			Documentation other = (Documentation) obj;
			return ClassUtils.equals(getId(), other.getId());
		}
		return false;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity#toString()
	 */
	@Override
	public String toString() {
		return "Documentation@"+String.valueOf(getId()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		if(content==null && resources.isEmpty()) {
			serializer.startEmptyElement(TAG_DOCUMENTATION);
		} else {
			serializer.startElement(TAG_DOCUMENTATION);
		}

		ModelXmlUtils.writeIdentityAttributes(serializer, this);

		if(content!=null) {
			serializer.startElement(TAG_CONTENT);
			serializer.writeCData(content);
			serializer.endElement(TAG_CONTENT);
		}

		for(Resource resource : resources) {
			ModelXmlUtils.writeResourceElement(serializer, resource);
		}

		serializer.endElement(TAG_DOCUMENTATION);
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_DOCUMENTATION: {
			ModelXmlUtils.readIdentity(attributes, this);
		} break;

		case TAG_RESOURCE: {
			return new ResourceImpl();
		}

		case TAG_CONTENT: {
			// no-op
		} break;

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_DOCUMENTATION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		case TAG_DOCUMENTATION: {
			return null;
		}

		case TAG_CONTENT: {
			setContent(text);
		} break;

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_DOCUMENTATION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		case TAG_RESOURCE: {
			addResource((Resource) handler);
		} break;

		default:
			throw new SAXException("Unrecognized nested tag  '"+qName+"' in "+TAG_DOCUMENTATION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

//	/**
//	 * @param target the target to set
//	 */
//	@Override
//	public void setTarget(Documentable target) {
//		this.target = target;
//	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Documentation#getContent()
	 */
	@Override
	public String getContent() {
		return content;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Documentation#getResources()
	 */
	@Override
	public List<Resource> getResources() {
		return CollectionUtils.getListProxy(resources);
	}

	/**
	 * @param content the content to set
	 */
//	@Override
	public void setContent(String content) {
		this.content = content;
	}

//	@Override
	public void addResource(Resource resource) {
		if (resource == null)
			throw new NullPointerException("Invalid resource"); //$NON-NLS-1$

		resources.add(resource);
	}

//	@Override
	public void removeResource(Resource resource) {
		if (resource == null)
			throw new NullPointerException("Invalid resource"); //$NON-NLS-1$

		resources.remove(resource);
	}

	public static class ResourceImpl extends DefaultModifiableIdentity implements Resource, ModelXmlElement, ModelXmlHandler {

		private Url url;

		public ResourceImpl() {
			// Default constructor
		}

		public ResourceImpl(String id, Url url) {
			setId(id);
			setUrl(url);
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
		 */
		@Override
		public void writeXml(XmlSerializer serializer) throws Exception {
			ModelXmlUtils.writeResourceElement(serializer, this);
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public ModelXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			switch (qName) {
			case TAG_RESOURCE: {
				ModelXmlUtils.readIdentity(attributes, this);
			} break;

			default:
				throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_RESOURCE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			case TAG_RESOURCE: {

				if(text!=null) {
					try {
						setUrl(new Url(text));
					} catch (MalformedURLException e) {
						throw new SAXException("Invalid resoucre url", e); //$NON-NLS-1$
					}
				}

				return null;
			}

			default:
				throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_RESOURCE+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}

		/**
		 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
		 */
		@Override
		public void endNestedHandler(ManifestLocation manifestLocation, String uri,
				String localName, String qName, ModelXmlHandler handler)
				throws SAXException {
			throw new UnsupportedOperationException();
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.Documentation.Resource#getUrl()
		 */
		@Override
		public Url getUrl() {
			return url;
		}

		/**
		 * @param url the url to set
		 */
		public void setUrl(Url url) {
			if (url == null)
				throw new NullPointerException("Invalid url"); //$NON-NLS-1$

			this.url = url;
		}

	}
}
