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

import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static de.ims.icarus.language.model.test.TestUtils.getTestValue;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.standard.manifest.ValueManifestImpl;
import de.ims.icarus.model.types.UnsupportedValueTypeException;
import de.ims.icarus.model.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueManifestImplTest implements ManifestTestConstants {

	private ValueManifestImpl manifest;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	@Before
	public void prepare() {
		manifest = new ValueManifestImpl(ValueType.STRING);
	}

	private void testType(final ValueType valueType) {
		ValueManifestImpl manifest = new ValueManifestImpl(valueType);

		assertSame("Value type corrupted", valueType, manifest.getValueType()); //$NON-NLS-1$

		Object value = getTestValue(valueType);

		manifest.setValue(value);

		assertEquals("Saved value mismatch", value, manifest.getValue()); //$NON-NLS-1$
	}

	private void testTypeXml(ValueType valueType) throws Exception {
		ValueManifestImpl manifest = new ValueManifestImpl(valueType);
		manifest.setName(TEST_NAME);
		manifest.setValue(getTestValue(valueType));

		assertSerializationEquals(manifest, new ValueManifestImpl(valueType));
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(manifest);
	}

	@Test
	public void testSetName() throws Exception {
		manifest.setName(TEST_NAME);
	}

	@Test(expected=NullPointerException.class)
	public void testSetNameNull() throws Exception {
		manifest.setName(null);
	}

	@Test
	public void testSetDescription() throws Exception {
		manifest.setDescription(TEST_DESCRIPTION);
	}

	@Test
	public void testSetDescriptionNull() throws Exception {
		manifest.setDescription(null);
	}

	@Test
	public void testSetValue() throws Exception {
		manifest.setValue("test-value"); //$NON-NLS-1$
	}

	@Test(expected=NullPointerException.class)
	public void testSetValueNull() throws Exception {
		manifest.setValue(null);
	}

	@Test
	public void testStringXml() throws Exception {
		testTypeXml(ValueType.STRING);
	}

	@Test
	public void testIntegerXml() throws Exception {
		testTypeXml(ValueType.INTEGER);
	}

	@Test
	public void testLongXml() throws Exception {
		testTypeXml(ValueType.LONG);
	}

	@Test
	public void testFloatXml() throws Exception {
		testTypeXml(ValueType.FLOAT);
	}

	@Test
	public void testDoubleXml() throws Exception {
		testTypeXml(ValueType.DOUBLE);
	}

	@Test
	public void testBooleanXml() throws Exception {
		testTypeXml(ValueType.BOOLEAN);
	}

	@Test
	public void testCustomXml() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testTypeXml(ValueType.CUSTOM);
	}

	@Test
	public void testUnknownXml() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testTypeXml(ValueType.UNKNOWN);
	}

	@Test
	public void testEnumXml() throws Exception {
		testTypeXml(ValueType.ENUM);
	}

	@Test
	public void testExtensionXml() throws Exception {
		testTypeXml(ValueType.EXTENSION);
	}

	@Test
	public void testImageXml() throws Exception {
		testTypeXml(ValueType.IMAGE);
	}

	@Test
	public void testImageResourceXml() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testTypeXml(ValueType.IMAGE_RESOURCE);
	}

	@Test
	public void testUrlXml() throws Exception {
		testTypeXml(ValueType.URL);
	}

	@Test
	public void testUrlResourceXml() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testTypeXml(ValueType.URL_RESOURCE);
	}

	@Test
	public void testString() throws Exception {
		testType(ValueType.STRING);
	}

	@Test
	public void testInteger() throws Exception {
		testType(ValueType.INTEGER);
	}

	@Test
	public void testLong() throws Exception {
		testType(ValueType.LONG);
	}

	@Test
	public void testFloat() throws Exception {
		testType(ValueType.FLOAT);
	}

	@Test
	public void testDouble() throws Exception {
		testType(ValueType.DOUBLE);
	}

	@Test
	public void testBoolean() throws Exception {
		testType(ValueType.BOOLEAN);
	}

	@Test
	public void testCustom() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testType(ValueType.CUSTOM);
	}

	@Test
	public void testUnknown() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testType(ValueType.UNKNOWN);
	}

	@Test
	public void testEnum() throws Exception {
		testType(ValueType.ENUM);
	}

	@Test
	public void testExtension() throws Exception {
		testType(ValueType.EXTENSION);
	}

	@Test
	public void testImage() throws Exception {
		testType(ValueType.IMAGE);
	}

	@Test
	public void testImageResource() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testType(ValueType.IMAGE_RESOURCE);
	}

	@Test
	public void testUrl() throws Exception {
		testType(ValueType.URL);
	}

	@Test
	public void testUrlResource() throws Exception {
		thrown.expect(UnsupportedValueTypeException.class);
		testType(ValueType.URL_RESOURCE);
	}
}
