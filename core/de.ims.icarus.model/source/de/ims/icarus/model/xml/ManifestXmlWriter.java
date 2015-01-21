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
package de.ims.icarus.model.xml;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.xml.stream.XmlStreamSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlWriter implements ModelXmlTags, ModelXmlAttributes {

	private final ManifestLocation manifestLocation;

	private final List<Manifest> manifests = new ArrayList<>();

	public ManifestXmlWriter(ManifestLocation manifestLocation) {
		if (manifestLocation == null)
			throw new NullPointerException("Invalid manifestLocation"); //$NON-NLS-1$

		this.manifestLocation = manifestLocation;
	}

	protected void checkManifest(Manifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(manifest.isTemplate()!=manifestLocation.isTemplate())
			throw new IllegalArgumentException("Manifest 'isTemplate' flag differs from value declared for writer: "+manifest.isTemplate()); //$NON-NLS-1$

		if(!(manifest instanceof ModelXmlElement))
			throw new IllegalArgumentException("Manifest is not serializable to xml: "+manifest.getId()); //$NON-NLS-1$
	}

	public void addManifest(Manifest manifest) {
		checkManifest(manifest);

		manifests.add(manifest);
	}

	public void addManifests(List<? extends Manifest> manifests) {
		if (manifests == null)
			throw new NullPointerException("Invalid manifests"); //$NON-NLS-1$

		for(Manifest manifest : manifests) {
			addManifest(manifest);
		}
	}

	public void writeAll() throws Exception {
		if(manifests.isEmpty()) {
			// Nothing to do here
			return;
		}

		XmlSerializer serializer = newSerializer(manifestLocation.getOutputStream());

		String rootTag = manifestLocation.isTemplate() ? TAG_TEMPLATES : TAG_CORPORA;

		serializer.startDocument();
		serializer.startElement(rootTag);

		for(Iterator<Manifest> it = manifests.iterator(); it.hasNext();) {
			ModelXmlElement element = (ModelXmlElement) it.next();

			element.writeXml(serializer);

			if(it.hasNext()) {
				serializer.writeLineBreak();
			}
		}

		serializer.endElement(rootTag);
		serializer.endDocument();
	}

	protected XmlSerializer newSerializer(OutputStream out) throws Exception {

		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "UTF-8"); //$NON-NLS-1$

		return new XmlStreamSerializer(writer);
	}
}
