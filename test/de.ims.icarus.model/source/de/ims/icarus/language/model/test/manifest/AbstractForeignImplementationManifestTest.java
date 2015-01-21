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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/AbstractForeignImplementationManifestTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertHashEquals;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdSetterSpec;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdentitySetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AbstractForeignImplementationManifest;
import de.ims.icarus.model.standard.manifest.ImplementationManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractForeignImplementationManifestTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class AbstractForeignImplementationManifestTest extends ManifestTestCase<AbstractForeignImplementationManifestTest.DummyManifest> {

	public static class DummyManifest extends AbstractForeignImplementationManifest<MemberManifest> {

		/**
		 * @param manifestLocation
		 * @param registry
		 */
		public DummyManifest(ManifestLocation manifestLocation,
				CorpusRegistry registry) {
			super(manifestLocation, registry);
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.Manifest#getManifestType()
		 */
		@Override
		public ManifestType getManifestType() {
			return ManifestType.DUMMY_MANIFEST;
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
		 */
		@Override
		protected String xmlTag() {
			return "test-manifest"; //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.AbstractForeignImplementationManifest#getImplementationManifest()
		 */
		@Override
		public ImplementationManifest getImplementationManifest() {
			return super.getImplementationManifest();
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
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	@Test
	public void testSetImplementationManifest() throws Exception {
		ImplementationManifest implementationManifest = mock(ImplementationManifest.class);

		manifest.setImplementationManifest(implementationManifest);

		assertSame(implementationManifest, manifest.getImplementationManifest());
	}

	@Test
	public void testSetImplementationManifestNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setImplementationManifest(null);
	}

	@Test
	public void testClearImplementationManifest() throws Exception {
		ImplementationManifest implementationManifest = mock(ImplementationManifest.class);

		manifest.setImplementationManifest(implementationManifest);

		assertSame(implementationManifest, manifest.getImplementationManifest());

		manifest.clearImplementationManifest();

		assertNull(manifest.getImplementationManifest());
	}

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlImplementation() throws Exception {
		manifest.setImplementationManifest(new ImplementationManifestImpl(location, registry));

		assertSerializationEquals(manifest, newInstance());
	}
}
