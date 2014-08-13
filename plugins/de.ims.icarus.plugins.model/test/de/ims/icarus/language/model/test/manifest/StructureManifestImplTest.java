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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.StructureType;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.StructureManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class StructureManifestImplTest extends ManifestTestCase<StructureManifestImpl> {

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected StructureManifestImpl newInstance() {
		return new StructureManifestImpl(location, registry, null);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getLayerManifest());
	}

	@Test
	public void testEquals() throws Exception {

		StructureManifestImpl other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	@Test
	public void testMissingEnvironment() throws Exception {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		when(manifestLocation.isTemplate()).thenReturn(false);
		CorpusRegistry registry = mock(CorpusRegistry.class);

		thrown.expect(ModelException.class);
		new StructureManifestImpl(manifestLocation, registry, null);
	}

	// MODIFICATION TESTS

	@Test
	public void testSetContainerType() throws Exception {
		for(ContainerType containerType : ContainerType.values()) {
			manifest.setContainerType(containerType);
			assertSame(containerType, manifest.getContainerType());
		}
	}

	@Test
	public void testSetStructureType() throws Exception {
		for(StructureType structureType : StructureType.values()) {
			manifest.setStructureType(structureType);
			assertSame(structureType, manifest.getStructureType());
		}
	}

	@Test
	public void testSetContainerTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setContainerType(null);
	}

	@Test
	public void testSetStructureTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setStructureType(null);
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		StructureManifestImpl template = newInstance();
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(StructureManifest.class, manifest, template);

		for(ContainerType containerType : ContainerType.values()) {
			template.setContainerType(containerType);
			assertTemplateGetters(StructureManifest.class, manifest, template);

			for(StructureType structureType : StructureType.values()) {
				template.setStructureType(structureType);
				assertTemplateGetters(StructureManifest.class, manifest, template);
			}
		}
	}

	// SERIALIZATION TESTS

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlType() throws Exception {
		for(ContainerType containerType : ContainerType.values()) {
			manifest.setContainerType(containerType);

			for(StructureType structureType : StructureType.values()) {
				manifest.setStructureType(structureType);
				assertSerializationEquals(manifest, newInstance());
			}
		}
	}
}
