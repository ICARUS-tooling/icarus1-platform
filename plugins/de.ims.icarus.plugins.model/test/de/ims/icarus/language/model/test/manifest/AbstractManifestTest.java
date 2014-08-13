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
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdSetterSpec;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AbstractManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractManifestTest extends ManifestTestCase<AbstractManifestTest.DummyManifest> {

	public static class DummyManifest extends AbstractManifest<Manifest> {

		/**
		 * @param manifestLocation
		 * @param registry
		 */
		public DummyManifest(ManifestLocation manifestLocation,
				CorpusRegistry registry) {
			super(manifestLocation, registry);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
		 */
		@Override
		protected String xmlTag() {
			return "test-manifest"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
		 */
		@Override
		protected boolean isEmpty() {
			// Big manifests should never be empty
			return false;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.Manifest#getManifestType()
		 */
		@Override
		public ManifestType getManifestType() {
			return ManifestType.DUMMY_MANIFEST;
		}

	}

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected DummyManifest newInstance() {
		return new DummyManifest(location, registry);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();
	}

	@Test
	public void testEquals() throws Exception {
		DummyManifest other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testXml() throws Exception {

		manifest.setId(TEST_ID);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testTemplate() throws Exception {

		DummyManifest template = newInstance();
		template.setId(TEST_TEMPLATE_ID);
		registry.registerTemplate(template);

		manifest.setTemplateId(TEST_TEMPLATE_ID);

		assertSame(template, manifest.getTemplate());
	}

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);
	}

	@Test
	public void testIsTemplate() throws Exception {
		manifest.setIsTemplate(true);
		assertTrue("Manifest flag on", manifest.isTemplate()); //$NON-NLS-1$

		manifest.setIsTemplate(false);
		assertFalse("Manifest flag off", manifest.isTemplate()); //$NON-NLS-1$
	}
}
