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
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdentitySetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AbstractLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AbstractLayerManifestTest extends ManifestTestCase<AbstractLayerManifestTest.DummyManifest> {

	public static class DummyManifest extends AbstractLayerManifest<LayerManifest> {

		/**
		 * @param manifestLocation
		 * @param registry
		 * @param layerGroupManifest
		 */
		public DummyManifest(ManifestLocation manifestLocation,
				CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
			super(manifestLocation, registry, layerGroupManifest);
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

	}

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected DummyManifest newInstance() {
		return new DummyManifest(location, registry, null);
	}

	private void fillBaseLayer(DummyManifest manifest) {
		manifest.addBaseLayerId(TEST_TEMPLATE_ID);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getGroupManifest());
	}

	@Test
	public void testEquals() throws Exception {

		DummyManifest other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testMissingEnvironment() throws Exception {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		when(manifestLocation.isTemplate()).thenReturn(false);
		CorpusRegistry registry = mock(CorpusRegistry.class);

		thrown.expect(ModelException.class);
		new DummyManifest(manifestLocation, registry, null);
	}

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	// MODIFICATION TESTS

	@Test
	public void testAddBaseLayer() throws Exception {

		LayerManifest target = mock(LayerManifest.class);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(TEST_TEMPLATE_ID)).thenReturn(target);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new DummyManifest(location, registry, groupManifest);

		TargetLayerManifest targetLayerManifest = manifest.addBaseLayerId(TEST_TEMPLATE_ID);

		assertSame(manifest, targetLayerManifest.getLayerManifest());

		List<TargetLayerManifest> list = manifest.getBaseLayerManifests();

		assertEquals(1, list.size());

		assertSame(targetLayerManifest, list.get(0));
		assertSame(target, targetLayerManifest.getResolvedLayerManifest());

		assertNull(targetLayerManifest.getPrerequisite());
	}

	@Test
	public void testSetLayerType() throws Exception {
		LayerType layerType = mock(LayerType.class);
		when(layerType.getId()).thenReturn(TEST_TEMPLATE_ID);

		registry.registerLayerType(layerType);

		manifest.setLayerTypeId(TEST_TEMPLATE_ID);

		assertSame(layerType, manifest.getLayerType());
	}

	@Test
	public void testSetLayerTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setLayerTypeId(null);
	}

	@Test
	public void testSetLayerTypeUnknown() throws Exception {
		manifest.setLayerTypeId("unknownLayerType"); //$NON-NLS-1$

		// Needs to do the getLayerType() call, since the actual type
		// reference is resolved lazily
		thrown.expect(IllegalArgumentException.class);
		manifest.getLayerType();
	}

	// SERIALIZATION TESTS

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlBaseLayer() throws Exception {

		// Fails due to lack of context
		thrown.expect(ModelException.class);

		fillBaseLayer(manifest);
		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlBaseLayerWithContext() throws Exception {

		LayerManifest target = mock(LayerManifest.class);
		when(target.getId()).thenReturn(TEST_TEMPLATE_ID);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(TEST_TEMPLATE_ID)).thenReturn(target);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new DummyManifest(location, registry, groupManifest);

		fillBaseLayer(manifest);

		DummyManifest other = new DummyManifest(location, registry, groupManifest);

		assertSerializationEquals(manifest, other);
	}
}
