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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/IndexManifestImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.IndexManifest.Coverage;
import de.ims.icarus.model.api.manifest.IndexManifest.Relation;
import de.ims.icarus.model.standard.manifest.IndexManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id: IndexManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class IndexManifestImplTest implements ManifestTestConstants {

	private IndexManifestImpl manifest;
	private DriverManifest driverManifest;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	@Before
	public void prepare() {
		driverManifest = mock(DriverManifest.class);

		manifest = new IndexManifestImpl(driverManifest);
	}

	@Test
	public void testObjectContract() throws Exception {
		assertObjectContract(manifest);
	}

	@Test
	public void testSetCoverageNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setCoverage(null);
	}

	@Test
	public void testSetCoverage() throws Exception {
		for(Coverage coverage : Coverage.values()) {
			manifest.setCoverage(coverage);

			assertSame(coverage, manifest.getCoverage());
		}
	}

	@Test
	public void testSetRelationNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setRelation(null);
	}

	@Test
	public void testSetRelation() throws Exception {
		for(Relation relation : Relation.values()) {
			manifest.setRelation(relation);

			assertSame(relation, manifest.getRelation());
		}
	}

	@Test
	public void testSetSourceLayerIdNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setSourceLayerId(null);
	}

	@Test
	public void testSetSourceLayerId() throws Exception {
		manifest.setSourceLayerId(TEST_ID);

		assertSame(TEST_ID, manifest.getSourceLayerId());
	}

	@Test
	public void testSetTargetLayerIdNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setTargetLayerId(null);
	}

	@Test
	public void testSetTargetLayerId() throws Exception {
		manifest.setTargetLayerId(TEST_ID);

		assertSame(TEST_ID, manifest.getTargetLayerId());
	}

	@Test
	public void testSetIncludeReverse() throws Exception {
		manifest.setIncludeReverse(true);

		assertTrue(manifest.isIncludeReverse());

		manifest.setIncludeReverse(false);
		assertFalse(manifest.isIncludeReverse());
	}

	@Test
	public void testXmlEmpty() throws Exception {
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlTargetLayerId() throws Exception {
		manifest.setTargetLayerId(TEST_ID);
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlTargetLayerIdNull() throws Exception {
		// Make sure source layer id is set
		manifest.setSourceLayerId(TEST_ID);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("target layer id"); //$NON-NLS-1$
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlSourceLayerId() throws Exception {
		manifest.setSourceLayerId(TEST_ID);
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlSourceLayerIdNull() throws Exception {
		// Make sure target layer id is set
		manifest.setTargetLayerId(TEST_ID);
		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("source layer id"); //$NON-NLS-1$
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlCoverage() throws Exception {
		manifest.setCoverage(Coverage.TOTAL);
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlRelation() throws Exception {
		manifest.setRelation(Relation.MANY_TO_MANY);
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
	}

	@Test
	public void testXmlFull() throws Exception {
		manifest.setTargetLayerId(TEST_ID+"1"); //$NON-NLS-1$
		manifest.setSourceLayerId(TEST_ID+"2"); //$NON-NLS-1$

		for(Coverage coverage : Coverage.values()) {
			for(Relation relation : Relation.values()) {

				manifest.setRelation(relation);
				manifest.setCoverage(coverage);

				assertSerializationEquals(manifest, new IndexManifestImpl(driverManifest));
			}
		}
	}
}
