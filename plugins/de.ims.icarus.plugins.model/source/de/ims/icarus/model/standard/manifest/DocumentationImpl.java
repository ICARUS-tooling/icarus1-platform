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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.model.api.manifest.Documentable;
import de.ims.icarus.model.api.manifest.Documentation;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DocumentationImpl extends DefaultModifiableIdentity implements Documentation {

	private final Documentable target;

	private String content;

	private final List<Resource> resources = new ArrayList<>();

	public DocumentationImpl(Documentable target) {
		if (target == null)
			throw new NullPointerException("Invalid target"); //$NON-NLS-1$

		this.target = target;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlElement#writeXml(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	public void writeXml(XmlSerializer serializer) throws Exception {
		serializer.startElement(TAG_DOCUMENTATION);

		ModelXmlUtils.writeIdentityAttributes(serializer, this);

		serializer.startElement(TAG_CONTENT);
		serializer.writeText(content);
		serializer.endElement(TAG_CONTENT);

		for(Resource resource : resources) {
			serializer.startElement(TAG_RESOURCE);
			ModelXmlUtils.writeIdentityAttributes(serializer, resource);
			serializer.writeText(resource.getURL().toExternalForm());
			serializer.endElement(TAG_RESOURCE);
		}

		serializer.endElement(TAG_DOCUMENTATION);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.Documentation#getTarget()
	 */
	@Override
	public Documentable getTarget() {
		return target;
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

	public static class ResourceImpl extends DefaultModifiableIdentity implements Resource {

		private URL url;

		/**
		 * @see de.ims.icarus.model.api.manifest.Documentation.Resource#getURL()
		 */
		@Override
		public URL getURL() {
			return url;
		}

		/**
		 * @param url the url to set
		 */
		public void setURL(URL url) {
			if (url == null)
				throw new NullPointerException("Invalid url"); //$NON-NLS-1$

			this.url = url;
		}

	}
}
