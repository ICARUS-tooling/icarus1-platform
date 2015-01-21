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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/DocumentationImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.getTestValues;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.api.manifest.Documentation.Resource;
import de.ims.icarus.model.standard.manifest.DocumentationImpl;
import de.ims.icarus.model.standard.manifest.DocumentationImpl.ResourceImpl;
import de.ims.icarus.model.types.Url;
import de.ims.icarus.model.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id: DocumentationImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class DocumentationImplTest implements ManifestTestConstants {

	private DocumentationImpl documentation;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	private static final String TEST_CONTENT =
			"This is some sort of test documentation.\n" //$NON-NLS-1$
			+ "It is intended as a multi line test with some illegal caharacters for" //$NON-NLS-1$
			+ "xml serialization like <,> and so on... !\"@§$%&/()=?"; //$NON-NLS-1$

	@Before
	public void prepare() {
		documentation = newInstance();
	}

	private DocumentationImpl newInstance() {
		return new DocumentationImpl();
	}

	private void fill(DocumentationImpl documentation) {
		documentation.setId(TEST_ID);
		documentation.setName(TEST_NAME);
		documentation.setDescription(TEST_DESCRIPTION);
		documentation.setIcon(TEST_ICON);
	}

	private void fillContent(DocumentationImpl documentation) {
		documentation.setContent(TEST_CONTENT);
	}

	private void fillResource(DocumentationImpl documentation) {
		documentation.addResource(new ResourceImpl("resource1", (Url)getTestValues(ValueType.URL)[0])); //$NON-NLS-1$
		documentation.addResource(new ResourceImpl("resource2", (Url)getTestValues(ValueType.URL)[1])); //$NON-NLS-1$
	}

	private void fillAll(DocumentationImpl documentation) throws Exception {
		fill(documentation);
		fillContent(documentation);
		fillResource(documentation);
	}

	@Test
	public void testSetId() throws Exception {
		documentation.setId(TEST_ID);

		assertSame(TEST_ID, documentation.getId());
	}

	@Test
	public void testSetName() throws Exception {
		documentation.setName(TEST_NAME);

		assertSame(TEST_NAME, documentation.getName());
	}

	@Test
	public void testSetDescription() throws Exception {
		documentation.setDescription(TEST_DESCRIPTION);

		assertSame(TEST_DESCRIPTION, documentation.getDescription());
	}

	@Test
	public void testSetIcon() throws Exception {
		documentation.setIcon(TEST_ICON);

		assertSame(TEST_ICON, documentation.getIcon());
	}

	@Test
	public void testSetContent() throws Exception {
		documentation.setContent(TEST_CONTENT);

		assertSame(TEST_CONTENT, documentation.getContent());
	}

	@Test
	public void testAddResource() throws Exception {
		Resource resource1 = new ResourceImpl("resource1", (Url)getTestValues(ValueType.URL)[0]); //$NON-NLS-1$
		Resource resource2 = new ResourceImpl("resource2", (Url)getTestValues(ValueType.URL)[1]); //$NON-NLS-1$
		Resource resource3 = new ResourceImpl("resource3", (Url)getTestValues(ValueType.URL)[2]); //$NON-NLS-1$

		documentation.addResource(resource1);
		documentation.addResource(resource2);
		documentation.addResource(resource3);

		List<Resource> resources = documentation.getResources();

		assertEquals(3, resources.size());

		assertSame(resource1, resources.get(0));
		assertSame(resource2, resources.get(1));
		assertSame(resource3, resources.get(2));
	}

	@Test
	public void testAddResourceNull() throws Exception {
		thrown.expect(NullPointerException.class);
		documentation.addResource(null);
	}

	@Test
	public void testRemoveResource() throws Exception {
		Resource resource1 = new ResourceImpl("resource1", (Url)getTestValues(ValueType.URL)[0]); //$NON-NLS-1$
		Resource resource2 = new ResourceImpl("resource2", (Url)getTestValues(ValueType.URL)[1]); //$NON-NLS-1$
		Resource resource3 = new ResourceImpl("resource3", (Url)getTestValues(ValueType.URL)[2]); //$NON-NLS-1$

		// Tested in other method above
		documentation.addResource(resource1);
		documentation.addResource(resource2);
		documentation.addResource(resource3);

		documentation.removeResource(resource2);

		List<Resource> resources = documentation.getResources();

		assertEquals(2, resources.size());

		assertSame(resource1, resources.get(0));
		assertSame(resource3, resources.get(1));
	}

	@Test
	public void testRemoveResourceNull() throws Exception {
		thrown.expect(NullPointerException.class);
		documentation.removeResource(null);
	}

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(documentation, newInstance());
	}

	@Test
	public void testXml() throws Exception {

		fill(documentation);

		assertSerializationEquals(documentation, newInstance());
	}

	@Test
	public void testXmlContent() throws Exception {

		fill(documentation);
		fillContent(documentation);

		assertSerializationEquals(documentation, newInstance());
	}

	@Test
	public void testXmlResource() throws Exception {

		fill(documentation);
		fillResource(documentation);

		assertSerializationEquals(documentation, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(documentation);

		assertSerializationEquals(documentation, newInstance());
	}
}
