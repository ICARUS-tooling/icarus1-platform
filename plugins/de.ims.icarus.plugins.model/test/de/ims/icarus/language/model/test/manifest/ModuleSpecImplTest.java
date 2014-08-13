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

import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdentitySetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec;
import de.ims.icarus.model.standard.manifest.DriverManifestImpl.ModuleSpecImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ModuleSpecImplTest implements ManifestTestConstants {

	private static final String EXTENSION_POINT_UID = "some.plugin.id@target-extension-point"; //$NON-NLS-1$

	private ModuleSpecImpl spec;
	private DriverManifest driverManifest;

	@Before
	public void prepare() {
		driverManifest = mock(DriverManifest.class);
		spec = newInstance();
	}

	protected ModuleSpecImpl newInstance() {
		return new ModuleSpecImpl(driverManifest);
	}

	private void fill(ModuleSpecImpl spec) {
		spec.setId(TEST_ID);
		spec.setName(TEST_NAME);
		spec.setDescription(TEST_DESCRIPTION);
		spec.setIcon(TEST_ICON);
	}

	private void fillOptional(ModuleSpecImpl spec) {
		spec.setOptional(!ModuleSpec.DEFAULT_IS_OPTIONAL);
	}

	private void fillCustomizable(ModuleSpecImpl spec) {
		spec.setCustomizable(!ModuleSpec.DEFAULT_IS_CUSTOMIZABLE);
	}

	private void fillExtensionPointUid(ModuleSpecImpl spec) {
		spec.setExtensionPointUid(EXTENSION_POINT_UID);
	}

	private void fillAll(ModuleSpecImpl spec) {
		fill(spec);
		fillCustomizable(spec);
		fillOptional(spec);
		fillExtensionPointUid(spec);
	}

	// GENERAL TESTS

	@Test
	public void testConsistency() throws Exception {
		assertSame(driverManifest, spec.getDriverManifest());
	}

	@Test(expected=NullPointerException.class)
	public void testConstructorNull() throws Exception {
		new ModuleSpecImpl(null);
	}

	@Test
	public void testEquals() throws Exception {
		spec.setId(TEST_ID);

		assertFalse(spec.equals(null));
		assertFalse(spec.equals(new Object()));

		ModuleSpecImpl other = newInstance();
		other.setId(TEST_ID+"2"); //$NON-NLS-1$

		assertFalse(spec.equals(other));

		other.setId(TEST_ID);
		assertTrue(spec.equals(other));

		assertTrue(spec.equals(spec));
	}

	// MODIFICATION TESTS

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdentitySetters(spec);
	}

	@Test
	public void testSetOptional() throws Exception {
		spec.setOptional(true);
		assertTrue(spec.isOptional());

		spec.setOptional(false);
		assertFalse(spec.isOptional());
	}

	@Test
	public void testSetCustomizable() throws Exception {
		spec.setCustomizable(true);
		assertTrue(spec.isCustomizable());

		spec.setCustomizable(false);
		assertFalse(spec.isCustomizable());
	}

	@Test
	public void testSetExtensionPointUid() throws Exception {
		spec.setExtensionPointUid(EXTENSION_POINT_UID);

		assertEquals(EXTENSION_POINT_UID, spec.getExtensionPointUid());
	}

	@Test
	public void testSetExtensionPointUidNull() throws Exception {
		spec.setExtensionPointUid(null);

		assertNull(spec.getExtensionPointUid());
	}

	// SERIALIZATION TESTS

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(spec, newInstance());
	}

	@Test
	public void testXml() throws Exception {

		fill(spec);

		assertSerializationEquals(spec, newInstance());
	}

	@Test
	public void testXmlOptional() throws Exception {

		fill(spec);
		fillOptional(spec);

		assertSerializationEquals(spec, newInstance());
	}

	@Test
	public void testXmlCustomizable() throws Exception {

		fill(spec);
		fillCustomizable(spec);

		assertSerializationEquals(spec, newInstance());
	}

	@Test
	public void testXmlExtensionPointUid() throws Exception {

		fill(spec);
		fillExtensionPointUid(spec);

		assertSerializationEquals(spec, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(spec);

		assertSerializationEquals(spec, newInstance());
	}
}
