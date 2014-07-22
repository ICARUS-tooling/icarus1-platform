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

import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdSetterSpec;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdentitySetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestInputLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.registry.CorpusRegistryImpl;
import de.ims.icarus.model.standard.manifest.AnnotationManifestImpl;
import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.model.util.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationManifestImplTest implements ManifestTestConstants {

	private static final ValueType DEFAULT_TYPE = ValueType.STRING;
	private static final String DEFAULT_KEY = "defaultKey"; //$NON-NLS-1$

	private AnnotationManifestImpl manifest;
	private ManifestLocation location;
	private CorpusRegistry registry;

	@Before
	public void prepare() {
		location = new VirtualManifestInputLocation(null, true);
		registry = new CorpusRegistryImpl();

		manifest = new AnnotationManifestImpl(location, registry);
	}

	// FILL

	private void fillAll(AnnotationManifestImpl manifest) {
		fill(manifest);
		fillProperties(manifest);
		fillAliases(manifest);
		fillValues(manifest);
		fillRange(manifest);
	}

	private void fill(AnnotationManifestImpl manifest) {
		manifest.setValueType(DEFAULT_TYPE);

		manifest.setId(LEGAL_ID);
		manifest.setName(TEST_NAME);
		manifest.setDescription(TEST_DESCRIPTION);
		manifest.setIcon(TEST_ICON);
		manifest.setKey(DEFAULT_KEY);
	}

	private void fillProperties(AnnotationManifestImpl manifest) {
		manifest.setProperty("property1", "property1Value"); //$NON-NLS-1$ //$NON-NLS-2$
		manifest.setProperty("property2", "property2Value"); //$NON-NLS-1$ //$NON-NLS-2$
		manifest.setProperty("property3", "property3Value"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void fillValues(AnnotationManifestImpl manifest) {
		ValueSetImpl values = new ValueSetImpl(DEFAULT_TYPE);
		values.addValue("Element 1"); //$NON-NLS-1$
		values.addValue("Element 2"); //$NON-NLS-1$
		values.addValue("Element 3"); //$NON-NLS-1$
		values.addValue("Element 4"); //$NON-NLS-1$
		values.addValue("Element 5"); //$NON-NLS-1$

		manifest.setSupportedValues(values);
	}

	private void fillRange(AnnotationManifestImpl manifest) {
		ValueRangeImpl range = new ValueRangeImpl(DEFAULT_TYPE);
		range.setLowerBound("aaaaaab"); //$NON-NLS-1$
		range.setUpperBound("zzzzzzy"); //$NON-NLS-1$
		range.setLowerBoundIncluded(false);

		manifest.setSupportedRange(range);
	}

	private void fillAliases(AnnotationManifestImpl manifest) {
		manifest.addAlias("someAlias"); //$NON-NLS-1$
		manifest.addAlias("AnotherFancyAlias"); //$NON-NLS-1$
	}

	// GENERAL TESTS

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	@Test(expected=NullPointerException.class)
	public void testValuesSetter() throws Exception {
		manifest.setSupportedValues(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRangeSetter() throws Exception {
		manifest.setSupportedRange(null);
	}

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		AnnotationManifestImpl template = new AnnotationManifestImpl(location, registry);
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertEquals("Template link ignored", template, manifest.getTemplate()); //$NON-NLS-1$

		assertEquals("Local id overwritten", TEST_ID, manifest.getId()); //$NON-NLS-1$

		assertEquals("Template name ignored", template.getName(), manifest.getName()); //$NON-NLS-1$

		assertEquals("Template description ignored", template.getDescription(), manifest.getDescription()); //$NON-NLS-1$

		assertEquals("Template icon ignored", template.getIcon(), manifest.getIcon()); //$NON-NLS-1$

		assertEquals("Template value-type ignored", template.getValueType(), manifest.getValueType()); //$NON-NLS-1$

		assertEquals("Template value-set ignored", template.getSupportedValues(), manifest.getSupportedValues()); //$NON-NLS-1$

		assertEquals("Template value-range ignored", template.getSupportedRange(), manifest.getSupportedRange()); //$NON-NLS-1$
	}

	// XML TEST

	// Should fail due to missing id and value type declaration
	@Test(expected=ModelException.class)
	public void testXmlEmpty() throws Exception {

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlSimple() throws Exception {

		fill(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlProperties() throws Exception {

		fill(manifest);
		fillProperties(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlAliases() throws Exception {

		fill(manifest);
		fillAliases(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlValues() throws Exception {

		fill(manifest);
		fillValues(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlRanges() throws Exception {

		fill(manifest);
		fillRange(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest);
	}
}
