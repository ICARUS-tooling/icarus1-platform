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

 * $Revision: 332 $
 * $Date: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/OptionImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertHashEquals;
import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static de.ims.icarus.language.model.test.TestUtils.getTestValue;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl.OptionImpl;
import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.model.types.UnsupportedValueTypeException;
import de.ims.icarus.model.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id: OptionImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class OptionImplTest implements ManifestTestConstants {

	private OptionImpl option;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	@Before
	public void prepare() {
		option = new OptionImpl(TEST_ID, ValueType.STRING);
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(option);
	}

	@Test
	public void testNullConstructor() throws Exception {
		thrown.expect(NullPointerException.class);
		new OptionImpl(INVALID_ID_NULL, ValueType.STRING);
	}

	private void testType(final ValueType valueType) throws Exception {
		option = new OptionImpl(TEST_ID, valueType);

		assertEquals("Saved valueType mismatch", valueType, option.getValueType()); //$NON-NLS-1$

		Object legalValue = getTestValue(valueType);
		Object illegalValue = new Object();

		option.setDefaultValue(legalValue);

		try {
			option.setDefaultValue(illegalValue);
			fail("Accepted object as "+valueType+" value"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(ModelException e) {
			// no-op
		}

		ValueSet legalValues = mock(ValueSet.class);
		when(legalValues.getValueType()).thenReturn(valueType);

		option.setSupportedValues(legalValues);

		ValueSet illegalValues = mock(ValueSet.class);
		when(illegalValues.getValueType()).thenReturn(ValueType.UNKNOWN);


		try {
			option.setSupportedValues(illegalValues);
			fail("Accepted object set as "+valueType+" value set"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	private void testTypeXml(ValueType valueType) throws Exception {
		option = new OptionImpl(TEST_ID, valueType);

		option.setDefaultValue(getTestValue(valueType));

		option.setSupportedValues(new ValueSetImpl(valueType));

		assertSerializationEquals(option, new OptionImpl(TEST_ID, valueType));
	}

	@Test
	public void testGeneralXml() throws Exception {

		option.setDescription(TEST_DESCRIPTION);
		option.setIcon(TEST_ICON);
		option.setMultiValue(!Option.DEFAULT_MULTIVALUE_VALUE);
		option.setPublished(!Option.DEFAULT_PUBLISHED_VALUE);

		assertSerializationEquals(option, new OptionImpl(TEST_ID, option.getValueType()));
	}

	@Test
	public void testEquals() throws Exception {
		OptionImpl option1 = new OptionImpl(TEST_ID, ValueType.STRING);
		OptionImpl option2 = new OptionImpl(TEST_ID+"234", ValueType.STRING); //$NON-NLS-1$

		assertHashEquals(option1, option2);

		assertHashEquals(option1, option1);
	}

	@Test
	public void testLegalId() throws Exception {
		option.setId(TEST_ID);
	}

	@Test
	public void testInvalidIdNull() throws Exception {
		thrown.expect(NullPointerException.class);
		option.setId(INVALID_ID_NULL);
	}

	@Test
	public void testInvalidIdLength() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setId(INVALID_ID_LENGTH);
	}

	@Test
	public void testInvalidIdContent() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setId(INVALID_ID_CONTENT);
	}

	@Test
	public void testInvalidIdBegin() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setId(INVALID_ID_BEGIN);
	}

	@Test
	public void testId() throws Exception {
		option.setId(TEST_ID);

		assertEquals("Saved id mismatch", TEST_ID, option.getId()); //$NON-NLS-1$
	}

	@Test
	public void testDescription() throws Exception {
		option.setDescription(TEST_DESCRIPTION);

		assertEquals("Saved description mismatch", TEST_DESCRIPTION, option.getDescription()); //$NON-NLS-1$
	}

	@Test
	public void testIcon() throws Exception {
		option.setIcon(TEST_ICON);

		assertEquals("Saved icon mismatch", TEST_ICON, option.getIcon()); //$NON-NLS-1$
	}

	@Test
	public void testGroup() throws Exception {
		option.setOptionGroup(TEST_ID);

		assertEquals("Saved group mismatch", TEST_ID, option.getOptionGroup()); //$NON-NLS-1$
	}

	@Test
	public void testName() throws Exception {
		option.setName(TEST_DESCRIPTION);

		assertEquals("Saved name mismatch", TEST_DESCRIPTION, option.getName()); //$NON-NLS-1$
	}

	@Test
	public void testPublished() throws Exception {
		option.setPublished(!Option.DEFAULT_PUBLISHED_VALUE);

		assertEquals("Saved published mismatch", !Option.DEFAULT_PUBLISHED_VALUE, option.isPublished()); //$NON-NLS-1$
	}

	@Test
	public void testMultiValue() throws Exception {
		option.setMultiValue(!Option.DEFAULT_MULTIVALUE_VALUE);

		assertEquals("Saved multiValue mismatch", !Option.DEFAULT_MULTIVALUE_VALUE, option.isMultiValue()); //$NON-NLS-1$
	}

	@Test
	public void testInvalidGroupNull() throws Exception {
		option.setOptionGroup(INVALID_ID_NULL);
	}

	@Test
	public void testInvalidGroupLength() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setOptionGroup(INVALID_ID_LENGTH);
	}

	@Test
	public void testInvalidGroupContent() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setOptionGroup(INVALID_ID_CONTENT);
	}

	@Test
	public void testInvalidGroupBegin() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setOptionGroup(INVALID_ID_BEGIN);
	}

	@Test
	public void testSetValues() throws Exception {
		option.setSupportedValues(null);
		assertNull(option.getSupportedValues());

		ValueSet valueSet = mock(ValueSet.class);
		when(valueSet.getValueType()).thenReturn(ValueType.STRING);

		option.setSupportedValues(valueSet);
		assertSame(valueSet, option.getSupportedValues());
	}

	@Test
	public void testSetValuesIncompatible() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setSupportedValues(new ValueSetImpl(ValueType.INTEGER));
	}

	@Test
	public void testSetRange() throws Exception {
		option.setSupportedRange(null);
		assertNull(option.getSupportedRange());

		ValueRange valueRange = mock(ValueRange.class);
		when(valueRange.getValueType()).thenReturn(ValueType.STRING);

		option.setSupportedRange(valueRange);
		assertSame(valueRange, option.getSupportedRange());
	}

	@Test
	public void testSetRangeIncompatible() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		option.setSupportedRange(new ValueRangeImpl(ValueType.INTEGER));
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
