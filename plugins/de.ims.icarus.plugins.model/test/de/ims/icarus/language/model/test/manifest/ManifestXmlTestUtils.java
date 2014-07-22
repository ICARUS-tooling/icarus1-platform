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
package de.ims.icarus.language.model.test.manifest;

import java.util.logging.Level;

import de.ims.icarus.logging.LogReport;
import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestInputLocation;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestOutputLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.registry.CorpusRegistryImpl;
import de.ims.icarus.model.xml.ManifestXmlWriter;
import de.ims.icarus.model.xml.sax.ManifestXmlReader;
import de.ims.icarus.util.classes.ClassUtils;
import de.ims.icarus.util.classes.ClassUtils.Trace;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlTestUtils {

	public static void assertSerializationEquals(Manifest manifest) throws Exception {
		ClassLoader classLoader = manifest.getClass().getClassLoader();
		boolean isTemplate = manifest.isTemplate();

		VirtualManifestOutputLocation outputLocation = new VirtualManifestOutputLocation(classLoader, isTemplate);
		ManifestXmlWriter writer = new ManifestXmlWriter(outputLocation);

		writer.addManifest(manifest);

		writer.writeAll();

		String xml = outputLocation.getContent();

//		System.out.println(xml);

		VirtualManifestInputLocation inputLocation = new VirtualManifestInputLocation(xml, classLoader, isTemplate);

		CorpusRegistry registry = new CorpusRegistryImpl();

		ManifestXmlReader reader = new ManifestXmlReader(registry);

		reader.addSource(inputLocation);

		LogReport report = reader.readAll();

		report.publish();

		if(report.hasRecordsAbove(Level.INFO)) {
			failForXml(xml, manifest);
		}

		Manifest newManifest = registry.getManifestsForSource(inputLocation).get(0);

		Trace trace = ClassUtils.deepDiff(manifest, newManifest);

		if(trace.hasMessages()) {
			failForTrace(trace, manifest);
		}
	}

	private static String getId(Manifest manifest) {
		String id = manifest.getId();
		if(id==null) {
			id = manifest.getClass()+"@<unnamed>"; //$NON-NLS-1$
		}
		return id;
	}

	private static void failForXml(String xml, Manifest manifest) {
		String message = getId(manifest);

		message += "  deserialization failed: \n"; //$NON-NLS-1$
		message += xml;

		throw new AssertionError(message, null);
	}

	private static void failForTrace(Trace trace, Manifest manifest) {
		String message = getId(manifest);

		message += " result of deserialization is different from original: \n"; //$NON-NLS-1$
		message += trace.getMessages();

		throw new AssertionError(message, null);
	}
}
