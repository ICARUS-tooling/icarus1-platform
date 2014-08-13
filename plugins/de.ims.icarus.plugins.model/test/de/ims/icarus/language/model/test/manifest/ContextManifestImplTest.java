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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AbstractLayerManifest;
import de.ims.icarus.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.ContextManifestImpl;
import de.ims.icarus.model.standard.manifest.ContextManifestImpl.PrerequisiteManifestImpl;
import de.ims.icarus.model.standard.manifest.FragmentLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.LayerGroupManifestImpl;
import de.ims.icarus.model.standard.manifest.LocationManifestImpl;
import de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContextManifestImplTest extends ManifestTestCase<ContextManifestImpl> {

	private static final String LAYER_ID_1 = "layer1"; //$NON-NLS-1$
	private static final String LAYER_ID_2 = "layer2"; //$NON-NLS-1$
	private static final String LAYER_ID_3 = "layer3"; //$NON-NLS-1$

	private static final String LAYER_ID_1_B = "layer1-b"; //$NON-NLS-1$
	private static final String LAYER_ID_2_B = "layer2-b"; //$NON-NLS-1$
	private static final String LAYER_ID_3_B = "layer3-b"; //$NON-NLS-1$

	private static final String GROUP_ID_1 = "layer-group-1"; //$NON-NLS-1$
	private static final String GROUP_ID_2 = "layer-group-2"; //$NON-NLS-1$

	private static final String PREREQUISITE_ALIAS_1 = "alias-1"; //$NON-NLS-1$
	private static final String PREREQUISITE_ALIAS_2 = "alias-2"; //$NON-NLS-1$

	private static final String FOREIGN_LAYER_ID_1 = "foreign-layer-1"; //$NON-NLS-1$
	private static final String FOREIGN_LAYER_ID_2 = "foreign-layer-2"; //$NON-NLS-1$
	private static final String FOREIGN_CONTEX_ID = "foreign-context"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected ContextManifestImpl newInstance() {
		return new ContextManifestImpl(location, registry);
	}

	private void fillGroups(ContextManifestImpl manifest) {

		AbstractLayerManifest<?> layerManifest;

		// GROUP 1

		LayerGroupManifestImpl groupManifest1 = new LayerGroupManifestImpl(manifest);
		groupManifest1.setId(GROUP_ID_1);

		layerManifest = new MarkableLayerManifestImpl(location, registry, groupManifest1);
		layerManifest.setId(LAYER_ID_1);
		groupManifest1.addLayerManifest(layerManifest);

		layerManifest = new MarkableLayerManifestImpl(location, registry, groupManifest1);
		layerManifest.setId(LAYER_ID_2);
		groupManifest1.addLayerManifest(layerManifest);

		layerManifest = new AnnotationLayerManifestImpl(location, registry, groupManifest1);
		layerManifest.setId(LAYER_ID_3);
		groupManifest1.addLayerManifest(layerManifest);

		groupManifest1.setPrimaryLayerId(LAYER_ID_2);
		manifest.addLayerGroup(groupManifest1);

		// GROUP 2

		LayerGroupManifestImpl groupManifest2 = new LayerGroupManifestImpl(manifest);
		groupManifest2.setId(GROUP_ID_2);

		layerManifest = new MarkableLayerManifestImpl(location, registry, groupManifest2);
		layerManifest.setId(LAYER_ID_1_B);
		groupManifest2.addLayerManifest(layerManifest);

		layerManifest = new AnnotationLayerManifestImpl(location, registry, groupManifest2);
		layerManifest.setId(LAYER_ID_2_B);
		groupManifest2.addLayerManifest(layerManifest);

		layerManifest = new FragmentLayerManifestImpl(location, registry, groupManifest2);
		layerManifest.setId(LAYER_ID_3_B);
		groupManifest2.addLayerManifest(layerManifest);

		groupManifest2.setPrimaryLayerId(LAYER_ID_1_B);
		manifest.addLayerGroup(groupManifest2);
	}

	private void fillPrerequisites(ContextManifestImpl manifest) {
		PrerequisiteManifestImpl prerequisiteManifest1 = manifest.addPrerequisite(PREREQUISITE_ALIAS_1, null);
		prerequisiteManifest1.setContextId(FOREIGN_CONTEX_ID);
		prerequisiteManifest1.setLayerId(FOREIGN_LAYER_ID_1);

		PrerequisiteManifestImpl prerequisiteManifest2 = manifest.addPrerequisite(PREREQUISITE_ALIAS_2, null);
		prerequisiteManifest2.setContextId(FOREIGN_CONTEX_ID);
		prerequisiteManifest2.setLayerId(FOREIGN_LAYER_ID_2);
	}

	private void fillPrimaryLayer(ContextManifestImpl manifest) {
		manifest.setPrimaryLayerId(LAYER_ID_2);
	}

	private void fillBaseLayer(ContextManifestImpl manifest) {
		manifest.setBaseLayerId(LAYER_ID_1);
	}

	private void fillLocation(ContextManifestImpl manifest) {
		manifest.setLocationManifest(new LocationManifestImpl(TEST_PATH));
	}

	private void fillAll(ContextManifestImpl manifest) {
		fillIdentity(manifest);
		fillGroups(manifest);
		fillPrerequisites(manifest);
		fillBaseLayer(manifest);
		fillPrimaryLayer(manifest);

		// NOTE: setting location requires the manifest to be in live version!
//		fillLocation(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getCorpusManifest());
	}

	@Test
	public void testEquals() throws Exception {

		ContextManifestImpl other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testRootContext() throws Exception {
		CorpusManifest corpusManifest = mock(CorpusManifest.class);

		manifest = new ContextManifestImpl(location, registry, corpusManifest);

		when(corpusManifest.getRootContextManifest()).thenReturn(manifest);

		assertTrue(manifest.isRootContext());
	}

	@Test
	public void testMissingEnvironment() throws Exception {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		when(manifestLocation.isTemplate()).thenReturn(false);
		CorpusRegistry registry = mock(CorpusRegistry.class);

		thrown.expect(ModelException.class);
		new ContextManifestImpl(manifestLocation, registry, null);
	}

	// MODIFICATION TESTS

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	@Test
	public void testAddLayerGroupNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addLayerGroup(null);
	}

	@Test
	public void testAddLayerGroupForeign() throws Exception {

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);

		thrown.expect(IllegalArgumentException.class);
		manifest.addLayerGroup(groupManifest);
	}

	@Test
	public void testAddLayerGroupDuplicate() throws Exception {

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(manifest);

		manifest.addLayerGroup(groupManifest);

		thrown.expect(IllegalArgumentException.class);
		manifest.addLayerGroup(groupManifest);
	}

	@Test
	public void testAddLayerGroup() throws Exception {

		LayerGroupManifest groupManifest1 = mock(LayerGroupManifest.class);
		when(groupManifest1.getContextManifest()).thenReturn(manifest);
		LayerGroupManifest groupManifest2 = mock(LayerGroupManifest.class);
		when(groupManifest2.getContextManifest()).thenReturn(manifest);
		LayerGroupManifest groupManifest3 = mock(LayerGroupManifest.class);
		when(groupManifest3.getContextManifest()).thenReturn(manifest);

		manifest.addLayerGroup(groupManifest1);
		manifest.addLayerGroup(groupManifest2);
		manifest.addLayerGroup(groupManifest3);

		List<LayerGroupManifest> list = manifest.getGroupManifests();

		assertEquals(3, list.size());

		assertSame(groupManifest1, list.get(0));
		assertSame(groupManifest2, list.get(1));
		assertSame(groupManifest3, list.get(2));
	}

	@Test
	public void testRemoveLayerGroupNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removeLayerGroup(null);
	}

	@Test
	public void testRemoveLayerGroup() throws Exception {

		LayerGroupManifest groupManifest1 = mock(LayerGroupManifest.class);
		when(groupManifest1.getContextManifest()).thenReturn(manifest);
		LayerGroupManifest groupManifest2 = mock(LayerGroupManifest.class);
		when(groupManifest2.getContextManifest()).thenReturn(manifest);
		LayerGroupManifest groupManifest3 = mock(LayerGroupManifest.class);
		when(groupManifest3.getContextManifest()).thenReturn(manifest);

		manifest.addLayerGroup(groupManifest1);
		manifest.addLayerGroup(groupManifest2);
		manifest.addLayerGroup(groupManifest3);

		manifest.removeLayerGroup(groupManifest2);

		List<LayerGroupManifest> list = manifest.getGroupManifests();

		assertEquals(2, list.size());

		assertSame(groupManifest1, list.get(0));
		assertSame(groupManifest3, list.get(1));

		assertFalse(list.contains(groupManifest2));
	}

	@Test
	public void testSetPrimaryLayer() throws Exception {

		fillGroups(manifest);
		fillPrimaryLayer(manifest);

		assertEquals(LAYER_ID_2, manifest.getPrimaryLayerManifest().getId());
	}

	@Test
	public void testSetBaseLayer() throws Exception {

		fillGroups(manifest);
		fillBaseLayer(manifest);

		assertEquals(LAYER_ID_1, manifest.getBaseLayerManifest().getId());
	}

	@Test
	public void testSetIndependent() throws Exception {
		manifest.setIndependentContext(true);
		assertTrue(manifest.isIndependentContext());

		manifest.setIndependentContext(false);
		assertFalse(manifest.isIndependentContext());
	}

	@Test
	public void testAddPrerequisiteNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addPrerequisite(null, null);
	}

	@Test
	public void testAddPrerequisiteDuplicate() throws Exception {
		manifest.addPrerequisite(PREREQUISITE_ALIAS_1, null);

		thrown.expect(IllegalArgumentException.class);
		manifest.addPrerequisite(PREREQUISITE_ALIAS_1, null);
	}

	@Test
	public void testAddPrerequisite() throws Exception {
		LayerManifest foreignLayer1 = mock(LayerManifest.class);
		when(foreignLayer1.getId()).thenReturn(FOREIGN_LAYER_ID_1);

		LayerManifest foreignLayer2 = mock(LayerManifest.class);
		when(foreignLayer2.getId()).thenReturn(FOREIGN_LAYER_ID_2);

		ContextManifest foreignContest = mock(ContextManifest.class);
		when(foreignContest.getId()).thenReturn(FOREIGN_CONTEX_ID);
		when(foreignContest.getLayerManifest(FOREIGN_LAYER_ID_1)).thenReturn(foreignLayer1);
		when(foreignContest.getLayerManifest(FOREIGN_LAYER_ID_2)).thenReturn(foreignLayer2);

		CorpusManifest corpusManifest = mock(CorpusManifest.class);
		when(corpusManifest.getContextManifest(FOREIGN_CONTEX_ID)).thenReturn(foreignContest);

		manifest = new ContextManifestImpl(location, registry, corpusManifest);
		fillIdentity(manifest);
		fillPrerequisites(manifest);

		assertSame("foreign layer 1", foreignLayer1, manifest.getLayerManifest(PREREQUISITE_ALIAS_1)); //$NON-NLS-1$
		assertSame("foreign layer 2", foreignLayer2, manifest.getLayerManifest(PREREQUISITE_ALIAS_2)); //$NON-NLS-1$
	}

	//TODO

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		ContextManifestImpl template = newInstance();
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(ContextManifest.class, manifest, template);
	}

	// SERIALIZATION TESTS

	@Test
	public void testXmlEmpty() throws Exception {
		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXml() throws Exception {

		fillIdentity(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlGroups() throws Exception {

		fillIdentity(manifest);
		fillGroups(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlPrerequisites() throws Exception {

		fillIdentity(manifest);
		fillPrerequisites(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlPrimaryLayer() throws Exception {

		fillIdentity(manifest);
		fillPrimaryLayer(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlBaseLayer() throws Exception {

		fillIdentity(manifest);
		fillBaseLayer(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlLocation() throws Exception {

		fillIdentity(manifest);
		thrown.expect(IllegalStateException.class);
		fillLocation(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlLocationLive() throws Exception {

		manifest.setIsTemplate(false);
		fillIdentity(manifest);
		fillLocation(manifest);

		ContextManifestImpl other = newInstance();
		other.setIsTemplate(false);

		assertSerializationEquals(manifest, other);
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, newInstance());
	}
}
