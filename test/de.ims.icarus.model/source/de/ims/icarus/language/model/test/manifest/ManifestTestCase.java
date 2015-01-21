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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/ManifestTestCase.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.api.manifest.Manifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestInputLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.registry.CorpusRegistryImpl;
import de.ims.icarus.model.standard.manifest.AbstractManifest;
import de.ims.icarus.model.standard.manifest.AbstractMemberManifest;

/**
 * @author Markus Gärtner
 * @version $Id: ManifestTestCase.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public abstract class ManifestTestCase<M extends Manifest> implements ManifestTestConstants {

	protected M manifest;
	protected ManifestLocation location;
	protected CorpusRegistry registry;

	protected abstract M newInstance();

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	protected void testConsistency() {
		assertSame("Manifest location mismatch ", location, manifest.getManifestLocation()); //$NON-NLS-1$

		assertSame("Manifest registry mismatch", registry, manifest.getRegistry()); //$NON-NLS-1$
	}

	@Before
	public void prepare() {
		location = new VirtualManifestInputLocation(null, true);
		registry = new CorpusRegistryImpl();

		manifest = newInstance();
	}

	protected void fillId(AbstractManifest<?> manifest) {
		manifest.setId(TEST_ID);
	}

	protected void fillIdentity(AbstractMemberManifest<?> manifest) {
		manifest.setId(TEST_ID);
		manifest.setName(TEST_NAME);
		manifest.setDescription(TEST_DESCRIPTION);
		manifest.setIcon(TEST_ICON);
	}
}
