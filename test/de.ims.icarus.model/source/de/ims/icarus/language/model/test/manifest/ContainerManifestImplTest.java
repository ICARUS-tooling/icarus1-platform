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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/ContainerManifestImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
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
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.members.ContainerType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.ContainerManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id: ContainerManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class ContainerManifestImplTest extends ManifestTestCase<ContainerManifestImpl> {

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected ContainerManifestImpl newInstance() {
		return new ContainerManifestImpl(location, registry, null);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getLayerManifest());
	}

	@Test
	public void testEquals() throws Exception {

		ContainerManifestImpl other = newInstance();

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
		new ContainerManifestImpl(manifestLocation, registry, null);
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
	public void testSetContainerTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setContainerType(null);
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		ContainerManifestImpl template = newInstance();
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(ContainerManifest.class, manifest, template);

		for(ContainerType containerType : ContainerType.values()) {
			template.setContainerType(containerType);
			assertTemplateGetters(ContainerManifest.class, manifest, template);
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
			assertSerializationEquals(manifest, newInstance());
		}
	}
}
