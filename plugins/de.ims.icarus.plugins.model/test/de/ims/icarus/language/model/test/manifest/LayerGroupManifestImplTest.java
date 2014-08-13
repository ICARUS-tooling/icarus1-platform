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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.standard.manifest.AbstractLayerManifest;
import de.ims.icarus.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.LayerGroupManifestImpl;
import de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LayerGroupManifestImplTest implements ManifestTestConstants {

	private static final String LAYER_ID_1 = "layer1"; //$NON-NLS-1$
	private static final String LAYER_ID_2 = "layer2"; //$NON-NLS-1$
	private static final String LAYER_ID_3 = "layer3"; //$NON-NLS-1$

	private static final String PRIMARY_LAYER_ID = LAYER_ID_1;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	private ContextManifest contextManifest;
	private LayerGroupManifestImpl manifest;

	@Before
	public void prepare() {
		contextManifest = mock(ContextManifest.class);
		when(contextManifest.getRegistry()).thenReturn(DEFAULT_REGISTRY);
		manifest = new LayerGroupManifestImpl(contextManifest);
	}

	private void fill(LayerGroupManifestImpl manifest) {
		manifest.setId(TEST_ID);
		manifest.setName(TEST_NAME);
		manifest.setDescription(TEST_DESCRIPTION);
		manifest.setIcon(TEST_ICON);
	}

	private void fillPrimaryLayer(LayerGroupManifestImpl manifest) {
		manifest.setPrimaryLayerId(PRIMARY_LAYER_ID);
	}

	private void fillLayers(LayerGroupManifestImpl manifest) {

		AbstractLayerManifest<?> layerManifest;

		layerManifest = new MarkableLayerManifestImpl(DEFAULT_TEMPLATE_LOCATION, DEFAULT_REGISTRY, manifest);
		layerManifest.setId(LAYER_ID_1);
		manifest.addLayerManifest(layerManifest);

		layerManifest = new MarkableLayerManifestImpl(DEFAULT_TEMPLATE_LOCATION, DEFAULT_REGISTRY, manifest);
		layerManifest.setId(LAYER_ID_2);
		manifest.addLayerManifest(layerManifest);

		layerManifest = new AnnotationLayerManifestImpl(DEFAULT_TEMPLATE_LOCATION, DEFAULT_REGISTRY, manifest);
		layerManifest.setId(LAYER_ID_3);
		manifest.addLayerManifest(layerManifest);
	}

	private void fillAll(LayerGroupManifestImpl manifest) {
		fill(manifest);
		fillLayers(manifest);
		fillPrimaryLayer(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testEquals() throws Exception {

		assertEquals(manifest, manifest);
		assertNotEquals(manifest, null);
		assertNotEquals(manifest, new Object());
	}

	// MODIFICATION TESTS

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdentitySetters(manifest);
	}

	@Test
	public void testSetPrimaryLayer() throws Exception {

		MarkableLayerManifest layerManifest = mock(MarkableLayerManifest.class);
		when(layerManifest.getId()).thenReturn(PRIMARY_LAYER_ID);

		manifest.addLayerManifest(layerManifest);

		manifest.setPrimaryLayerId(PRIMARY_LAYER_ID);

		assertSame(layerManifest, manifest.getPrimaryLayerManifest());
	}

	@Test
	public void testAddLayer() throws Exception {
		LayerManifest layerManifest1 = mock(LayerManifest.class);
		when(layerManifest1.getId()).thenReturn(LAYER_ID_1);

		LayerManifest layerManifest2 = mock(LayerManifest.class);
		when(layerManifest2.getId()).thenReturn(LAYER_ID_2);

		LayerManifest layerManifest3 = mock(LayerManifest.class);
		when(layerManifest3.getId()).thenReturn(LAYER_ID_3);


		manifest.addLayerManifest(layerManifest1);
		manifest.addLayerManifest(layerManifest2);
		manifest.addLayerManifest(layerManifest3);

		List<LayerManifest> list = manifest.getLayerManifests();

		assertEquals(3, list.size());

		assertSame(layerManifest1, list.get(0));
		assertSame(layerManifest2, list.get(1));
		assertSame(layerManifest3, list.get(2));

		assertSame(layerManifest1, manifest.getLayerManifest(LAYER_ID_1));
		assertSame(layerManifest2, manifest.getLayerManifest(LAYER_ID_2));
		assertSame(layerManifest3, manifest.getLayerManifest(LAYER_ID_3));
	}

	@Test
	public void testAddLayerNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addLayerManifest(null);
	}

	@Test
	public void testRemoveLayer() throws Exception {
		LayerManifest layerManifest1 = mock(LayerManifest.class);
		when(layerManifest1.getId()).thenReturn(LAYER_ID_1);

		LayerManifest layerManifest2 = mock(LayerManifest.class);
		when(layerManifest2.getId()).thenReturn(LAYER_ID_2);

		LayerManifest layerManifest3 = mock(LayerManifest.class);
		when(layerManifest3.getId()).thenReturn(LAYER_ID_3);


		manifest.addLayerManifest(layerManifest1);
		manifest.addLayerManifest(layerManifest2);
		manifest.addLayerManifest(layerManifest3);

		manifest.removeLayerManifest(layerManifest2);

		List<LayerManifest> list = manifest.getLayerManifests();

		assertEquals(2, list.size());

		assertSame(layerManifest1, list.get(0));
		assertSame(layerManifest3, list.get(1));

		assertFalse(list.contains(layerManifest2));
	}

	@Test
	public void testRemoveLayerUnknown() throws Exception {
		LayerManifest layerManifest = mock(LayerManifest.class);

		thrown.expect(IllegalArgumentException.class);
		manifest.removeLayerManifest(layerManifest);
	}

	@Test
	public void testRemoveLayerNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removeLayerManifest(null);
	}

	@Test
	public void testSetIndependent() throws Exception {
		manifest.setIndependent(true);
		assertTrue(manifest.isIndependent());

		manifest.setIndependent(false);
		assertFalse(manifest.isIndependent());
	}

	// SERIALIZATION TESTS

	@Test
	public void testXmlEmpty() throws Exception {

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("primary layer id"); //$NON-NLS-1$
		assertSerializationEquals(manifest, new LayerGroupManifestImpl(contextManifest) );
	}

	@Test
	public void testXml() throws Exception {

		fill(manifest);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("primary layer id"); //$NON-NLS-1$
		assertSerializationEquals(manifest, new LayerGroupManifestImpl(contextManifest));
	}

	@Test
	public void testXmlLayers() throws Exception {

		fill(manifest);
		fillLayers(manifest);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("primary layer id"); //$NON-NLS-1$
		assertSerializationEquals(manifest, new LayerGroupManifestImpl(contextManifest));
	}

	@Test
	public void testXmlPrimaryLayer() throws Exception {

		fill(manifest);
		fillPrimaryLayer(manifest);

		assertSerializationEquals(manifest, new LayerGroupManifestImpl(contextManifest));
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, new LayerGroupManifestImpl(contextManifest));
	}

}
