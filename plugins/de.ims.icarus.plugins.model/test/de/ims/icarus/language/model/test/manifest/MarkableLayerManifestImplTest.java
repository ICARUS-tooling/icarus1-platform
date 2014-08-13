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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.ContainerType;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.standard.manifest.ContainerManifestImpl;
import de.ims.icarus.model.standard.manifest.MarkableLayerManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerManifestImplTest extends ManifestTestCase<MarkableLayerManifestImpl> {

	private static final String BOUNDARY_LAYER_ID = "boundary-layer-id"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected MarkableLayerManifestImpl newInstance() {
		return new MarkableLayerManifestImpl(location, registry, null);
	}

	private void fillContainer(MarkableLayerManifestImpl manifest) {
		ContainerManifestImpl containerManifest1 = new ContainerManifestImpl(
				manifest.getManifestLocation(), manifest.getRegistry(), manifest);
		containerManifest1.setContainerType(ContainerType.LIST);

		manifest.addContainerManifest(containerManifest1);

		ContainerManifestImpl containerManifest2 = new ContainerManifestImpl(
				manifest.getManifestLocation(), manifest.getRegistry(), manifest);
		containerManifest2.setContainerType(ContainerType.SPAN);

		manifest.addContainerManifest(containerManifest2);
	}

	private void fillBoundaryLayer(MarkableLayerManifestImpl manifest) {
		manifest.setBoundaryLayerId(BOUNDARY_LAYER_ID);
	}

	private void fillAll(MarkableLayerManifestImpl manifest) {
		fillIdentity(manifest);
		fillContainer(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getGroupManifest());
	}

	@Test
	public void testEquals() throws Exception {

		MarkableLayerManifestImpl other = newInstance();

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
	public void testSetBoundaryLayer() throws Exception {

		LayerManifest target = mock(LayerManifest.class);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(BOUNDARY_LAYER_ID)).thenReturn(target);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new MarkableLayerManifestImpl(location, registry, groupManifest);

		TargetLayerManifest targetLayerManifest = manifest.setBoundaryLayerId(BOUNDARY_LAYER_ID);

		assertSame(manifest, targetLayerManifest.getLayerManifest());

		TargetLayerManifest savedTargetLayerManifest = manifest.getBoundaryLayerManifest();


		assertSame(targetLayerManifest, savedTargetLayerManifest);
		assertSame(target, savedTargetLayerManifest.getResolvedLayerManifest());

		assertNull(targetLayerManifest.getPrerequisite());
	}

	@Test
	public void testSetBoundaryLayerNull() throws Exception {

		ContextManifest contextManifest = mock(ContextManifest.class);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new MarkableLayerManifestImpl(location, registry, groupManifest);

		thrown.expect(NullPointerException.class);
		manifest.setBoundaryLayerId(null);
	}

	@Test
	public void testSetBoundaryLayerWithoutContext() throws Exception {
		thrown.expect(ModelException.class);
		manifest.setBoundaryLayerId(BOUNDARY_LAYER_ID);
	}

	@Test
	public void testAddContainerManifest() throws Exception {
		ContainerManifest containerManifest1 = mock(ContainerManifest.class);
		ContainerManifest containerManifest2 = mock(ContainerManifest.class);
		ContainerManifest containerManifest3 = mock(ContainerManifest.class);

		manifest.addContainerManifest(containerManifest1);
		manifest.addContainerManifest(containerManifest2);
		manifest.addContainerManifest(containerManifest3);

		assertEquals(3, manifest.getContainerDepth());

		assertSame(containerManifest1, manifest.getContainerManifest(0));
		assertSame(containerManifest2, manifest.getContainerManifest(1));
		assertSame(containerManifest3, manifest.getContainerManifest(2));

		assertSame(containerManifest1, manifest.getRootContainerManifest());
	}

	@Test
	public void testAddContainerManifestNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addContainerManifest(null);
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		MarkableLayerManifestImpl template = newInstance();
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(MarkableLayerManifest.class, manifest, template);
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
	public void testXmlContainer() throws Exception {

		fillIdentity(manifest);
		fillContainer(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlBoundaryLayer() throws Exception {

		LayerManifest target = mock(LayerManifest.class);
		when(target.getId()).thenReturn(BOUNDARY_LAYER_ID);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(BOUNDARY_LAYER_ID)).thenReturn(target);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new MarkableLayerManifestImpl(location, registry, groupManifest);

		fillIdentity(manifest);

		fillBoundaryLayer(manifest);

		MarkableLayerManifestImpl newInstance = new MarkableLayerManifestImpl(location, registry, groupManifest);

		assertSerializationEquals(manifest, newInstance);
	}

	@Test
	public void testXmlFull() throws Exception {

		LayerManifest target = mock(LayerManifest.class);
		when(target.getId()).thenReturn(BOUNDARY_LAYER_ID);

		ContextManifest contextManifest = mock(ContextManifest.class);
		when(contextManifest.getLayerManifest(BOUNDARY_LAYER_ID)).thenReturn(target);

		LayerGroupManifest groupManifest = mock(LayerGroupManifest.class);
		when(groupManifest.getContextManifest()).thenReturn(contextManifest);

		manifest = new MarkableLayerManifestImpl(location, registry, groupManifest);

		fillAll(manifest);

		MarkableLayerManifestImpl newInstance = new MarkableLayerManifestImpl(location, registry, groupManifest);

		assertSerializationEquals(manifest, newInstance);
	}

}
