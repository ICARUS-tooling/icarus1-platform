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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/ModuleManifestImplTest.java $
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
import static org.mockito.Mockito.when;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec;
import de.ims.icarus.model.standard.manifest.DriverManifestImpl.ModuleManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id: ModuleManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class ModuleManifestImplTest extends ManifestTestCase<ModuleManifestImpl> {

	private static final String MODULE_SPEC_ID = "moduleSpecId"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected ModuleManifestImpl newInstance() {
		return new ModuleManifestImpl(location, registry, null);
	}

	private void fill(ModuleManifestImpl manifest) {
		fillIdentity(manifest);
	}

	private void fillModuleSpecId(ModuleManifestImpl manifest) {
		manifest.setModuleSpecId(MODULE_SPEC_ID);
	}

	private void fillAll(ModuleManifestImpl manifest) {
		fill(manifest);
		fillModuleSpecId(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getDriverManifest());
	}

	@Test
	public void testEquals() throws Exception {

		ModuleManifestImpl other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	// MODIFICATION TESTS

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	@Test
	public void testSetModuleSpecId() throws Exception {
		ModuleSpec spec = mock(ModuleSpec.class);
		when(spec.getId()).thenReturn(MODULE_SPEC_ID);

		DriverManifest driverManifest = mock(DriverManifest.class);
		when(driverManifest.getModuleSpec(MODULE_SPEC_ID)).thenReturn(spec);

		manifest = new ModuleManifestImpl(location, registry, driverManifest);

		manifest.setModuleSpecId(MODULE_SPEC_ID);

		assertSame(spec, manifest.getModuleSpec());
	}

	// SERIALIZATION TESTS

	@Test
	public void testXmlEmpty() throws Exception {

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXml() throws Exception {

		fill(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlModuleSpecId() throws Exception {

		fill(manifest);
		fillModuleSpecId(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, newInstance());
	}
}
