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
package de.ims.icarus.language.model.test.xml;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import de.ims.icarus.language.model.meta.ValueType;
import de.ims.icarus.language.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.language.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.language.model.standard.manifest.Values;
import de.ims.icarus.language.model.xml.XmlSerializer;
import de.ims.icarus.language.model.xml.XmlWriter;
import de.ims.icarus.language.model.xml.sax.ManifestParser;
import de.ims.icarus.language.model.xml.stream.XmlStreamSerializer;
import de.ims.icarus.logging.LogReport;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ModelXmlTest {

//	@Test
	public void testStreamSerializer() throws Exception {

		File file = new File("temp/streamTest.xml");

		XMLOutputFactory factory = XMLOutputFactory.newFactory();

		XMLStreamWriter writer = factory.createXMLStreamWriter(new FileOutputStream(file), "UTF-8");
//		XMLStreamWriter writer = factory.createXMLStreamWriter(System.out, "UTF-8");

		XmlSerializer serializer = new XmlStreamSerializer(writer);


		// Create model

		OptionsManifestImpl template = new OptionsManifestImpl();

		template.setId("tpl:test:options");

		template.addOption("cores");
		template.setValueType("cores", ValueType.INTEGER);
		template.setDefaultValue("cores", 1);
		template.setRange("cores", Values.newValueRange(1, 10));
		template.setName("cores", "#Cores");
		template.setDescription("cores", "Number of cores to use for calculation");

		OptionsManifestImpl optionsManifest = new OptionsManifestImpl(template);

		optionsManifest.setId("def:test:options");

		optionsManifest.addOption("print");
		optionsManifest.setValueType("print", ValueType.BOOLEAN);
		optionsManifest.setDefaultValue("print", true);
		optionsManifest.setValues("print", new ValueSetImpl(CollectionUtils.asList(Boolean.TRUE, Boolean.FALSE)));
		optionsManifest.setName("print", "Print Data");
		optionsManifest.setDescription("print", "Choose whether or not data should be printed");

		// Serialize model

		serializer.startDocument();
		XmlWriter.writeOptionsManifestElement(serializer, template);
		XmlWriter.writeOptionsManifestElement(serializer, optionsManifest);
		serializer.endDocument();

		serializer.close();
	}

	@Test
	public void testModelTemplateParser() throws Exception {

		File file = new File("temp/streamInTest1.xml");

		LogReport report = ManifestParser.getInstance().loadTemplates(file.toURI().toURL());
		report.publish();
	}

	@Test
	public void testModelCorporaParser() throws Exception {

		File file = new File("temp/streamInTest2.xml");

		LogReport report = ManifestParser.getInstance().loadCorpora(file.toURI().toURL());
		report.publish();
	}
}
