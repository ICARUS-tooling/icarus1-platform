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
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.standard.manifest.ContainerManifestImpl;
import de.ims.icarus.model.standard.manifest.FragmentLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.RasterizerManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class FragmentLayerManifestImplTest extends ManifestTestCase<FragmentLayerManifestImpl> {

	private static final String VALUE_LAYER_ID = "value-layer-id"; //$NON-NLS-1$
	private static final String ANNOTATION_KEY = "annotation-key"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected FragmentLayerManifestImpl newInstance() {
		return new FragmentLayerManifestImpl(location, registry, null);
	}

	private void fillValueLayer(FragmentLayerManifestImpl manifest) {
		manifest.setValueLayerId(VALUE_LAYER_ID);
	}

	private void fillAnnotationKey(FragmentLayerManifestImpl manifest) {
		manifest.setAnnotationKey(ANNOTATION_KEY);
	}

	private void fillRasterizer(FragmentLayerManifestImpl manifest) {
		manifest.setRasterizerManifest(new RasterizerManifestImpl(
				manifest.getManifestLocation(), manifest.getRegistry()));
	}

	private void fillContainer(FragmentLayerManifestImpl manifest) {
		ContainerManifestImpl container = new ContainerManifestImpl(location, registry, manifest);
		container.setContainerType(ContainerType.LIST);
		manifest.addContainerManifest(container);
	}

	private void fillAll(FragmentLayerManifestImpl manifest) {
		fillIdentity(manifest);
		fillRasterizer(manifest);
		fillAnnotationKey(manifest);
		fillContainer(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getGroupManifest());
		assertNull(manifest.getRasterizerManifest());
		assertNull(manifest.getAnnotationKey());
	}

	@Test
	public void testEquals() throws Exception {

		FragmentLayerManifestImpl other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);

		assertIdentitySetters(manifest);
	}

	// MODIFICATION TESTS

	@Test
	public void testSetValueLayerNull() throws Exception {

		ContextManifest contextManifest = mock(ContextManifest.class);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new FragmentLayerManifestImpl(location, registry, groupManifest);

		thrown.expect(NullPointerException.class);
		manifest.setValueLayerId(null);
	}

	@Test
	public void testSetValueLayer() throws Exception {

		AnnotationLayerManifest valueLayer = mock(AnnotationLayerManifest.class);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(VALUE_LAYER_ID)).thenReturn(valueLayer);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new FragmentLayerManifestImpl(location, registry, groupManifest);

		manifest.setValueLayerId(VALUE_LAYER_ID);

		assertSame(valueLayer, manifest.getValueLayerManifest().getResolvedLayerManifest());
	}

	@Test
	public void testSetAnnotationy() throws Exception {
		manifest.setAnnotationKey(ANNOTATION_KEY);
		assertSame(ANNOTATION_KEY, manifest.getAnnotationKey());
	}

	@Test
	public void testSetAnnotationKeyNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setAnnotationKey(null);
	}

	@Test
	public void testSetRasterizer() throws Exception {
		RasterizerManifest rasterizerManifest = mock(RasterizerManifest.class);

		manifest.setRasterizerManifest(rasterizerManifest);
		assertSame(rasterizerManifest, manifest.getRasterizerManifest());
	}

	@Test
	public void testSetRasterizerNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setRasterizerManifest(null);
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		FragmentLayerManifestImpl template = newInstance();
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(FragmentLayerManifest.class, manifest, template);
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
	public void testXmlValueLayer() throws Exception {

		fillIdentity(manifest);

		thrown.expect(ModelException.class);
		fillValueLayer(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlValueLayerWithEnvironment() throws Exception {

		// Assigning a value layer requires a valid layer group environment

		AnnotationLayerManifest valueLayer = mock(AnnotationLayerManifest.class);
		when(valueLayer.getId()).thenReturn(VALUE_LAYER_ID);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(VALUE_LAYER_ID)).thenReturn(valueLayer);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new FragmentLayerManifestImpl(location, registry, groupManifest);

		fillIdentity(manifest);
		fillValueLayer(manifest);

		FragmentLayerManifestImpl newInstance = new FragmentLayerManifestImpl(location, registry, groupManifest);

		assertSerializationEquals(manifest, newInstance);
	}

	@Test
	public void testXmlAnnotationKey() throws Exception {

		fillIdentity(manifest);
		fillAnnotationKey(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlRasterizer() throws Exception {

		fillIdentity(manifest);
		fillRasterizer(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlContainer() throws Exception {

		fillIdentity(manifest);
		fillContainer(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		// Assigning a value layer requires a valid layer group environment

		AnnotationLayerManifest valueLayer = mock(AnnotationLayerManifest.class);
		when(valueLayer.getId()).thenReturn(VALUE_LAYER_ID);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(VALUE_LAYER_ID)).thenReturn(valueLayer);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new FragmentLayerManifestImpl(location, registry, groupManifest);

		fillAll(manifest);
		fillValueLayer(manifest);

		FragmentLayerManifestImpl newInstance = new FragmentLayerManifestImpl(location, registry, groupManifest);

		assertSerializationEquals(manifest, newInstance);
	}
}
