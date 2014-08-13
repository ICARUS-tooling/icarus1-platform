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

import static org.junit.Assert.fail;

import java.io.OutputStream;
import java.util.logging.Level;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import de.ims.icarus.logging.LogReport;
import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestInputLocation;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestOutputLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.registry.CorpusRegistryImpl;
import de.ims.icarus.model.xml.ManifestXmlWriter;
import de.ims.icarus.model.xml.ModelXmlElement;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.model.xml.sax.DelegatingHandler;
import de.ims.icarus.model.xml.sax.ManifestXmlReader;
import de.ims.icarus.model.xml.stream.XmlStreamSerializer;
import de.ims.icarus.util.classes.ClassUtils;
import de.ims.icarus.util.classes.ClassUtils.Trace;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlTestUtils {


	/**
	 * Heavyweight assertion method. The given {@code Manifest} is first serialized via
	 * a freshly created {@link ManifestXmlWriter}, using a virtual {@link ManifestLocation}
	 * that stores the result of an xml serialization as a string. Then another virtual
	 * {@code ManifestLocation} is created from the xml string, this time to be used with
	 * an instance of {@link ManifestXmlReader} to deserialize the manifest. In case the
	 * reader's log contains entries with a log level of {@link Level#WARNING} or higher,
	 * the assertion will fail.
	 * <p>
	 * The original manifest and the result of the aforementioned processing chain are then
	 * compared in depth via the {@link ClassUtils#deepDiff(Object, Object)} method. In case
	 * the resulting {@link Trace  object contains any message entries that indicate differences
	 * between the two objects, an {@code AssertionError} is thrown.
	 *
	 * @param manifest
	 * @throws Exception
	 */
	public static void assertSerializationEquals(String msg, Manifest manifest) throws Exception {
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
			failForXml(msg, xml, manifest);
		}

		Manifest newManifest = registry.getManifestsForSource(inputLocation).get(0);

		Trace trace = ClassUtils.deepDiff(manifest, newManifest);

		if(trace.hasMessages()) {
			failForTrace(msg, trace, manifest, xml);
		}
	}

	public static void assertSerializationEquals(Manifest manifest) throws Exception {
		assertSerializationEquals(null, manifest);
	}

	public static void assertSerializationEquals(ModelXmlElement element, ModelXmlHandler handler) throws Exception {
		checkSerialization(null, element, handler, true);
	}

	public static void assertSerializationNotEquals(ModelXmlElement element, ModelXmlHandler handler) throws Exception {
		checkSerialization(null, element, handler, false);
	}

	public static void assertSerializationEquals(String msg, ModelXmlElement element, ModelXmlHandler handler) throws Exception {
		checkSerialization(msg, element, handler, true);
	}

	public static void assertSerializationNotEquals(String msg, ModelXmlElement element, ModelXmlHandler handler) throws Exception {
		checkSerialization(msg, element, handler, false);
	}

	private static void checkSerialization(String msg, ModelXmlElement element, ModelXmlHandler handler, boolean equals) throws Exception {

		// Environment stuff
		ClassLoader classLoader = element.getClass().getClassLoader();
		boolean isTemplate = true;

		// Create virtual output location
		VirtualManifestOutputLocation outputLocation = new VirtualManifestOutputLocation(classLoader, isTemplate);

		// Create serializer
		XmlSerializer serializer = createSerializer(outputLocation.getOutputStream());

		// Serialize element
		serializer.startDocument();
		element.writeXml(serializer);
		serializer.endDocument();

		String xml = outputLocation.getContent();

		VirtualManifestInputLocation inputLocation = new VirtualManifestInputLocation(xml, classLoader, isTemplate);

		// Read in serialized content of original element into handler object
		XMLReader reader = newReader();

		DelegatingHandler delegatingHandler = new DelegatingHandler(inputLocation, handler);

		reader.setContentHandler(delegatingHandler);
		reader.setDTDHandler(delegatingHandler);
		reader.setEntityResolver(delegatingHandler);
		reader.setErrorHandler(delegatingHandler);

		InputSource inputSource = new InputSource(inputLocation.getInputStream());
		inputSource.setEncoding("UTF-8"); //$NON-NLS-1$

		reader.parse(inputSource);

		// Collect all the differences
		Trace trace = ClassUtils.deepDiff(element, handler);

		if(equals && trace.hasMessages()) {
			failForTrace(msg, trace, element, xml);
		} else if(!equals && !trace.hasMessages()) {
			failForEqual(msg, element, handler);
		}
	}

	private static XmlSerializer createSerializer(OutputStream out) throws Exception {
		XMLOutputFactory factory = XMLOutputFactory.newFactory();
		XMLStreamWriter writer = factory.createXMLStreamWriter(out, "UTF-8"); //$NON-NLS-1$
		return new XmlStreamSerializer(writer);
	}

	private static XMLReader newReader() throws Exception {
		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		parserFactory.setNamespaceAware(true);
//		parserFactory.setValidating(true);
		//FIXME

		parserFactory.setFeature("http://xml.org/sax/features/use-entity-resolver2", true); //$NON-NLS-1$


		SAXParser parser = parserFactory.newSAXParser();

		return parser.getXMLReader();
	}

	private static String getId(Object manifest) {
		String id = null;

		if(manifest instanceof Manifest) {
			id = ((Manifest)manifest).getId();
		} else if(manifest instanceof Identity) {
			id = ((Identity)manifest).getId();
		}

		if(id==null) {
			id = manifest.getClass()+"@<unnamed>"; //$NON-NLS-1$
		}
		return id;
	}

	private static void failForXml(String msg, String xml, Manifest manifest) {
		String message = getId(manifest);

		message += "  deserialization failed: \n"; //$NON-NLS-1$
		message += xml;

		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}

		fail(message);
	}

	private static void failForEqual(String msg, Object original, Object created) {
		String message = "Expected result of deserialization to be different from original"; //$NON-NLS-1$
		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}
		fail(message);
	}

	private static void failForTrace(String msg, Trace trace, Object manifest, String xml) {
		String message = getId(manifest);

		message += " result of deserialization is different from original: \n"; //$NON-NLS-1$
		message += trace.getMessages();
		message += " {serialized form: "+xml.replaceAll("\\s{2,}", "")+"}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		if(msg!=null) {
			message = msg+": "+message; //$NON-NLS-1$
		}

		fail(message);
	}
}
