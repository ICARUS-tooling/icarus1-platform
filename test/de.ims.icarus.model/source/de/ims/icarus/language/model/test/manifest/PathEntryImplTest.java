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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/PathEntryImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.api.manifest.LocationManifest.PathType;
import de.ims.icarus.model.standard.manifest.LocationManifestImpl.PathEntryImpl;

/**
 * @author Markus Gärtner
 * @version $Id: PathEntryImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class PathEntryImplTest implements ManifestTestConstants {

	private PathEntryImpl pathEntry;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	@Before
	public void prepare() {
		pathEntry = new PathEntryImpl();
	}

	private void testTypeXml(PathType type) throws Exception {
		pathEntry.setType(type);
		pathEntry.setValue(TEST_PATH);

		assertSerializationEquals(pathEntry, new PathEntryImpl());
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(pathEntry);
	}

	@Test
	public void testSetType() throws Exception {
		pathEntry.setType(PathType.FILE);

		assertSame(PathType.FILE, pathEntry.getType());
	}

	@Test
	public void testSetTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		pathEntry.setType(null);
	}

	@Test
	public void testSetValue() throws Exception {
		pathEntry.setValue(TEST_PATH);

		assertSame(TEST_PATH, pathEntry.getValue());
	}

	@Test
	public void testSetValueNull() throws Exception {
		thrown.expect(NullPointerException.class);
		pathEntry.setValue(null);
	}

	@Test
	public void testXmlEmpty() throws Exception {
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(pathEntry, new PathEntryImpl());
	}

	@Test
	public void testXmlValue() throws Exception {
		pathEntry.setValue(TEST_PATH);

		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(pathEntry, new PathEntryImpl());
	}

	@Test
	public void testXmlType() throws Exception {
		pathEntry.setType(PathType.FILE);

		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(pathEntry, new PathEntryImpl());
	}

	@Test
	public void testXmlCustom() throws Exception {
		testTypeXml(PathType.CUSTOM);
	}

	@Test
	public void testXmlFile() throws Exception {
		testTypeXml(PathType.FILE);
	}

	@Test
	public void testXmlFolder() throws Exception {
		testTypeXml(PathType.FOLDER);
	}

	@Test
	public void testXmlIdentifier() throws Exception {
		testTypeXml(PathType.IDENTIFIER);
	}

	@Test
	public void testXmlPattern() throws Exception {
		testTypeXml(PathType.PATTERN);
	}
}
