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

import static de.ims.icarus.language.model.test.TestUtils.assertHashEquals;
import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static de.ims.icarus.language.model.test.TestUtils.getTestValues;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.model.util.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueSetImplTest {

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	private void testAdd(ValueType valueType) throws Exception {

		ValueSetImpl valueSet = new ValueSetImpl(valueType);

		assertSame("Value type corrupted", valueType, valueSet.getValueType()); //$NON-NLS-1$

		Object[] values = getTestValues(valueType);

		assertNotNull("Test value array empty", values); //$NON-NLS-1$

		for(Object value : values) {
			valueSet.addValue(value);
		}

		for(int i=0; i<values.length; i++) {
			assertSame("Mismatching item at index "+i, values[i], valueSet.getValueAt(i)); //$NON-NLS-1$
		}
	}

	private void testXml(ValueType valueType) throws Exception {

		ValueSetImpl valueSet = new ValueSetImpl(valueType);
		Object[] values = getTestValues(valueType);

		assertNotNull("Test value array empty", values); //$NON-NLS-1$

		for(Object value : values) {
			valueSet.addValue(value);
		}

		ValueSetImpl newSet = new ValueSetImpl(valueType);

		assertSerializationEquals(valueSet, newSet);
	}

	@Test
	public void testGeneral() throws Exception {
		ValueSetImpl valueSet1 = new ValueSetImpl(ValueType.STRING);
		ValueSetImpl valueSet2 = new ValueSetImpl(ValueType.INTEGER);

		assertHashEquals(valueSet1, valueSet2);
		assertHashEquals(valueSet1, valueSet1);
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(new ValueSetImpl(ValueType.STRING));
	}

	@Test
	public void testAddNull() throws Exception {
		ValueSetImpl valueSet = new ValueSetImpl(ValueType.STRING);

		thrown.expect(NullPointerException.class);
		valueSet.addValue(null);
	}

	// CONSTRUCTION

	@Test
	public void testStringSet() throws Exception {
		testAdd(ValueType.STRING);
	}

	@Test
	public void testIntegerSet() throws Exception {
		testAdd(ValueType.INTEGER);
	}

	@Test
	public void testLongSet() throws Exception {
		testAdd(ValueType.LONG);
	}

	@Test
	public void testFloatSet() throws Exception {
		testAdd(ValueType.FLOAT);
	}

	@Test
	public void testDoubleSet() throws Exception {
		testAdd(ValueType.DOUBLE);
	}

	@Test
	public void testEnumSet() throws Exception {
		testAdd(ValueType.ENUM);
	}

	@Test
	public void testExtensionSet() throws Exception {
		testAdd(ValueType.EXTENSION);
	}

	@Test
	public void testImageSet() throws Exception {
		testAdd(ValueType.IMAGE);
	}

	@Test
	public void testImageResourceSet() throws Exception {
		testAdd(ValueType.IMAGE_RESOURCE);
	}

	@Test
	public void testBooleanSet() throws Exception {
		testAdd(ValueType.BOOLEAN);
	}

	@Test
	public void testUrlSet() throws Exception {
		testAdd(ValueType.URL);
	}

	@Test
	public void testUrlResourceSet() throws Exception {
		testAdd(ValueType.URL_RESOURCE);
	}

	@Test
	public void testUnknownSet() throws Exception {
		testAdd(ValueType.UNKNOWN);
	}

	@Test
	public void testCustomSet() throws Exception {
		testAdd(ValueType.CUSTOM);
	}

	//SERIALIZATION

	@Test
	public void testXmlStringSet() throws Exception {
		testXml(ValueType.STRING);
	}

	@Test
	public void testXmlIntegerSet() throws Exception {
		testXml(ValueType.INTEGER);
	}

	@Test
	public void testXmlLongSet() throws Exception {
		testXml(ValueType.LONG);
	}

	@Test
	public void testXmlFloatSet() throws Exception {
		testXml(ValueType.FLOAT);
	}

	@Test
	public void testXmlDoubleSet() throws Exception {
		testXml(ValueType.DOUBLE);
	}

	@Test
	public void testXmlEnumSet() throws Exception {
		testXml(ValueType.ENUM);
	}

	@Test
	public void testXmlExtensionSet() throws Exception {
		testXml(ValueType.EXTENSION);

		// Use modified value type that bypasses the plugin engine
//		testXml(TestUtils.EXTENSION_TYPE);
	}

	@Test
	public void testXmlImageSet() throws Exception {
		testXml(ValueType.IMAGE);
	}

	@Test
	public void testXmlImageResourceSet() throws Exception {
		thrown.expect(UnsupportedOperationException.class);
		testXml(ValueType.IMAGE_RESOURCE);
	}

	@Test
	public void testXmlBooleanSet() throws Exception {
		testXml(ValueType.BOOLEAN);
	}

	//FIXME tests disabled due to blocking internet call by URL.equals(Object) method!
//	@Test
//	public void testXmlUrlSet() throws Exception {
//		testXml(ValueType.URL);
//	}

	@Test
	public void testXmlUrlResourceSet() throws Exception {
		thrown.expect(UnsupportedOperationException.class);
		testXml(ValueType.URL_RESOURCE);
	}

	@Test
	public void testXmlUnknownSet() throws Exception {
		thrown.expect(UnsupportedOperationException.class);
		testXml(ValueType.UNKNOWN);
	}

	@Test
	public void testXmlCustomSet() throws Exception {
		thrown.expect(UnsupportedOperationException.class);
		testXml(ValueType.CUSTOM);
	}
}
