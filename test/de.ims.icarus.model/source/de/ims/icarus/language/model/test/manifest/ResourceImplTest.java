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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/ResourceImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static de.ims.icarus.language.model.test.TestUtils.getTestValue;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.standard.manifest.DocumentationImpl.ResourceImpl;
import de.ims.icarus.model.types.Url;
import de.ims.icarus.model.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id: ResourceImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class ResourceImplTest implements ManifestTestConstants {

	private ResourceImpl resource;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	@Before
	public void prepare() {
		resource = new ResourceImpl();
	}

	private void fill(ResourceImpl resource) {
		resource.setId(TEST_ID);
		resource.setDescription(TEST_DESCRIPTION);
		resource.setName(TEST_NAME);
		resource.setIcon(TEST_ICON);
	}

	private void fillURL(ResourceImpl resource) {
		resource.setUrl((Url) getTestValue(ValueType.URL));
	}

	private void fillAll(ResourceImpl resource) {
		fill(resource);
		fillURL(resource);
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(resource);
	}

	@Test
	public void testSetId() throws Exception {
		resource.setId(TEST_ID);

		assertSame(TEST_ID, resource.getId());
	}

	@Test
	public void testSetName() throws Exception {
		resource.setName(TEST_NAME);

		assertSame(TEST_NAME, resource.getName());
	}

	@Test
	public void testSetDescription() throws Exception {
		resource.setDescription(TEST_DESCRIPTION);

		assertSame(TEST_DESCRIPTION, resource.getDescription());
	}

	@Test
	public void testSetIcon() throws Exception {
		resource.setIcon(TEST_ICON);

		assertSame(TEST_ICON, resource.getIcon());
	}

	@Test
	public void testSetUrl() throws Exception {
		Url url = (Url) getTestValue(ValueType.URL);

		resource.setUrl(url);

		assertSame(url, resource.getUrl());
	}

	@Test
	public void testSetUrlNull() throws Exception {
		thrown.expect(NullPointerException.class);
		resource.setUrl(null);
	}

	// Bound to fail due to missing URL
	@Test
	public void testXmlEmpty() throws Exception {
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(resource, new ResourceImpl());
	}

	// Bound to fail due to missing URL
	@Test
	public void testXml() throws Exception {

		fill(resource);

		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(resource, new ResourceImpl());
	}

	@Test
	public void testXmlURL() throws Exception {

		fillURL(resource);

		assertSerializationEquals(resource, new ResourceImpl());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(resource);

		assertSerializationEquals(resource, new ResourceImpl());
	}
}
