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

import de.ims.icarus.model.api.manifest.LocationManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.PathResolverManifest;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationManifestImpl implements LocationManifest, ModelXmlHandler {
	private String path;
	private PathResolverManifest pathResolverManifest;

	private final List<PathEntry> pathEntries = new ArrayList<>();

	/**
	 * @param attributes
	 */
	protected void readAttributes(Attributes attributes) {
		path = ModelXmlUtils.normalize(attributes, ATTR_PATH);
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_LOCATION: {
			readAttributes(attributes);
		} break;

		case TAG_PATH_ENTRY : {
			return new PathEntryImpl();
		}

		default:
			throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_LOCATION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_LOCATION: {
			return null;
		}

		default:
			throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_LOCATION+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_PATH_ENTRY : {
			addPathEntry((PathEntry) handler);
		} break;

		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LocationManifest#getPath()
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LocationManifest#getPathResolverManifest()
	 */
	@Override
	public PathResolverManifest getPathResolverManifest() {
		return pathResolverManifest;
	}

	/**
	 * @param path the path to set
	 */
	@Override
	public void setPath(String path) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.isEmpty())
			throw new IllegalArgumentException("Empty path"); //$NON-NLS-1$

		this.path = path;
	}

	@Override
	public void setPathResolverManifest(PathResolverManifest pathResolverManifest) {
		if (pathResolverManifest == null)
			throw new NullPointerException("Invalid pathResolverManifest"); //$NON-NLS-1$

		this.pathResolverManifest = pathResolverManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LocationManifest#getPathEntries()
	 */
	@Override
	public List<PathEntry> getPathEntries() {
		return CollectionUtils.getListProxy(pathEntries);
	}

	@Override
	public void addPathEntry(PathEntry entry) {
		if (entry == null)
			throw new NullPointerException("Invalid entry"); //$NON-NLS-1$

		pathEntries.add(entry);
	}

	@Override
	public void removePathEntry(PathEntry entry) {
		if (entry == null)
			throw new NullPointerException("Invalid entry"); //$NON-NLS-1$

		pathEntries.remove(entry);
	}

	public static class PathEntryImpl implements PathEntry, ModelXmlHandler {

		private PathType type;
		private String value;

		protected PathEntryImpl() {
			// no-op
		}

		public PathEntryImpl(PathType type, String value) {
			if (type == null)
				throw new NullPointerException("Invalid type"); //$NON-NLS-1$
			if (value == null)
				throw new NullPointerException("Invalid value"); //$NON-NLS-1$

			this.type = type;
			this.value = value;
		}

		/**
		 * @param attributes
		 */
		protected void readAttributes(Attributes attributes) {
			String typeId = ModelXmlUtils.normalize(attributes, ATTR_TYPE);
			type = PathType.parsePathType(typeId);
		}

		@Override
		public ModelXmlHandler startElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, Attributes attributes)
						throws SAXException {
			switch (qName) {
			case TAG_PATH_ENTRY: {
				readAttributes(attributes);
			} break;

			default:
				throw new SAXException("Unrecognized opening tag  '"+qName+"' in "+TAG_PATH_ENTRY+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			return this;
		}

		@Override
		public ModelXmlHandler endElement(ManifestLocation manifestLocation,
				String uri, String localName, String qName, String text)
						throws SAXException {
			switch (qName) {
			case TAG_PATH_ENTRY: {
				return null;
			}

			default:
				throw new SAXException("Unrecognized end tag  '"+qName+"' in "+TAG_PATH_ENTRY+" environment"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		 * @see de.ims.icarus.model.api.manifest.LocationManifest.PathEntry#getType()
		 */
		@Override
		public PathType getType() {
			return type;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LocationManifest.PathEntry#getValue()
		 */
		@Override
		public String getValue() {
			return value;
		}

	}
}
