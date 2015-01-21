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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/RasterizerManifestImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertTemplateGetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.standard.manifest.ImplementationManifestImpl;
import de.ims.icarus.model.standard.manifest.RasterizerManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id: RasterizerManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class RasterizerManifestImplTest extends ManifestTestCase<RasterizerManifestImpl> {

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected RasterizerManifestImpl newInstance() {
		return new RasterizerManifestImpl(location, registry);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getImplementationManifest());
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		ImplementationManifest implementationManifest = mock(ImplementationManifest.class);

		// Prepare template
		RasterizerManifestImpl template = newInstance();
		template.setId(TEST_TEMPLATE_ID);
		template.setImplementationManifest(implementationManifest);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(RasterizerManifest.class, manifest, template);
	}

	// SERIALIZATION TESTS

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
