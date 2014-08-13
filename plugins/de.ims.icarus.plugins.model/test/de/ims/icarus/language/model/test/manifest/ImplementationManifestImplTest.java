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
import static de.ims.icarus.language.model.test.TestUtils.assertTemplateGetters;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdSetterSpec;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdentitySetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest.SourceType;
import de.ims.icarus.model.standard.manifest.ImplementationManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ImplementationManifestImplTest extends ManifestTestCase<ImplementationManifestImpl> {

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected ImplementationManifestImpl newInstance() {
		return new ImplementationManifestImpl(location, registry);
	}

	private void fillSourceType(ImplementationManifestImpl manifest) {
		// Using DEFAULT here breaks the test
		manifest.setSourceType(SourceType.EXTERN);
	}

	private void fillSource(ImplementationManifestImpl manifest) {
		manifest.setSource(TEST_PATH);
	}

	private void fillClassname(ImplementationManifestImpl manifest) {
		manifest.setClassname(TEST_PATH);
	}

	private void fillUseFactory(ImplementationManifestImpl manifest) {
		manifest.setUseFactory(!ImplementationManifest.DEFAULT_USE_FACTORY_VALUE);
	}

	private void fillAll(ImplementationManifestImpl manifest) {
		fillIdentity(manifest);
		fillClassname(manifest);
		fillSourceType(manifest);
		fillSource(manifest);
		fillUseFactory(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();
	}

	@Test
	public void testEquals() throws Exception {

		ImplementationManifestImpl other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	@Test
	public void testSetSourceType() throws Exception {
		for(SourceType sourceType : SourceType.values()) {
			manifest.setSourceType(sourceType);

			assertSame(sourceType, manifest.getSourceType());
		}
	}

	@Test
	public void testSetSourceTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setSourceType(null);
	}

	@Test
	public void testSetUseFactory() throws Exception {
		manifest.setUseFactory(true);

		assertTrue(manifest.isUseFactory());

		manifest.setUseFactory(false);

		assertFalse(manifest.isUseFactory());
	}

	@Test
	public void testSetSource() throws Exception {
		manifest.setSource(null);
		manifest.setSource(TEST_PATH);

		assertSame(TEST_PATH, manifest.getSource());
	}

	@Test
	public void testSetClassname() throws Exception {
		manifest.setClassname(null);
		manifest.setClassname(TEST_PATH);

		assertSame(TEST_PATH, manifest.getClassname());
	}

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		ImplementationManifestImpl template = newInstance();
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(ImplementationManifest.class, manifest, template);
	}

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXml() throws Exception {

		fillIdentity(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlSourceType() throws Exception {

		fillIdentity(manifest);
		fillSourceType(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlSource() throws Exception {

		fillIdentity(manifest);
		fillSource(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlClassname() throws Exception {

		fillIdentity(manifest);
		fillClassname(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlUseFactory() throws Exception {

		fillIdentity(manifest);
		fillUseFactory(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, newInstance());
	}
}
