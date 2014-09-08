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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.util.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ValueRangeImplTest {

	@Rule
	public ExpectedException thrown= ExpectedException.none();

	private void testBounds(final ValueType valueType, final Object stepSize) {

		ValueRangeImpl valueRange = new ValueRangeImpl(valueType);

		assertSame("Value type", valueType, valueRange.getValueType()); //$NON-NLS-1$

		Object[] values = getTestValues(valueType);

		assertNotNull("Test value array", values); //$NON-NLS-1$

		assertFalse("Test values array too small", values.length<2); //$NON-NLS-1$

		Object lower = values[0];
		Object upper = values[1];

		valueRange.setLowerBound(lower);
		assertSame(lower, valueRange.getLowerBound());


		valueRange.setUpperBound(upper);
		assertSame(upper, valueRange.getUpperBound());

		if(stepSize!=null) {
			valueRange.setStepSize(stepSize);
			assertSame(stepSize, valueRange.getStepSize());
		}
	}

	private void testRangeXml(final ValueType valueType, final Object stepSize) throws Exception {

		ValueRangeImpl valueRange = new ValueRangeImpl(valueType);

		Object[] values = getTestValues(valueType);

		assertNotNull("Test value array", values); //$NON-NLS-1$
		Object lower = values[0];
		Object upper = values[1];

		valueRange.setLowerBound(lower);
		valueRange.setUpperBound(upper);

		if(stepSize!=null) {
			valueRange.setStepSize(stepSize);
		}

		assertSerializationEquals(valueRange, new ValueRangeImpl(valueType));
	}

	@Test
	public void testGeneral() throws Exception {
		ValueRangeImpl valueRange1 = new ValueRangeImpl(ValueType.STRING);
		ValueRangeImpl valueRange2 = new ValueRangeImpl(ValueType.INTEGER);

		assertHashEquals(valueRange1, valueRange2);
		assertHashEquals(valueRange1, valueRange1);
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(new ValueRangeImpl(ValueType.STRING));
	}

	@Test
	public void testBoundsIncluded() throws Exception {
		ValueRangeImpl valueRange = new ValueRangeImpl(ValueType.STRING);

		valueRange.setLowerBoundIncluded(true);
		assertTrue(valueRange.isLowerBoundInclusive());

		valueRange.setLowerBoundIncluded(false);
		assertFalse(valueRange.isLowerBoundInclusive());

		valueRange.setUpperBoundIncluded(true);
		assertTrue(valueRange.isUpperBoundInclusive());

		valueRange.setUpperBoundIncluded(false);
		assertFalse(valueRange.isUpperBoundInclusive());
	}

	@Test
	public void testLowerBoundNull() throws Exception {
		ValueRangeImpl valueRange = new ValueRangeImpl(ValueType.STRING);

		thrown.expect(NullPointerException.class);
		valueRange.setLowerBound(null);
	}

	@Test
	public void testUpperBoundNull() throws Exception {
		ValueRangeImpl valueRange = new ValueRangeImpl(ValueType.STRING);

		thrown.expect(NullPointerException.class);
		valueRange.setUpperBound(null);
	}

	@Test
	public void testStepSizeNull() throws Exception {
		ValueRangeImpl valueRange = new ValueRangeImpl(ValueType.STRING);

		thrown.expect(NullPointerException.class);
		valueRange.setStepSize(null);
	}

	// BASICS

	@Test
	public void testStringRange() throws Exception {
		testBounds(ValueType.STRING, null);
	}

	@Test
	public void testIntegerRange() throws Exception {
		testBounds(ValueType.INTEGER, 1);
	}

	@Test
	public void testLongRange() throws Exception {
		testBounds(ValueType.LONG, 1L);
	}

	@Test
	public void testFloatRange() throws Exception {
		testBounds(ValueType.FLOAT, 0.001F);
	}

	@Test
	public void testDoubleRange() throws Exception {
		testBounds(ValueType.DOUBLE, 0.001);
	}

	@Test
	public void testEnumRange() throws Exception {
		testBounds(ValueType.ENUM, null);
	}

	// XML

	@Test
	public void testStringRangeXml() throws Exception {
		testRangeXml(ValueType.STRING, null);
	}

	@Test
	public void testIntegerRangeXml() throws Exception {
		testRangeXml(ValueType.INTEGER, 1);
	}

	@Test
	public void testLongRangeXml() throws Exception {
		testRangeXml(ValueType.LONG, 1L);
	}

	@Test
	public void testFloatRangeXml() throws Exception {
		testRangeXml(ValueType.FLOAT, 0.001F);
	}

	@Test
	public void testDoubleRangeXml() throws Exception {
		testRangeXml(ValueType.DOUBLE, 0.001);
	}

	@Test
	public void testEnumRangeXml() throws Exception {
		testRangeXml(ValueType.ENUM, null);
	}
}
