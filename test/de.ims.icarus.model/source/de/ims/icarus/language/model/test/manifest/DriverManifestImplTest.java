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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/DriverManifestImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertHashEquals;
import static de.ims.icarus.language.model.test.TestUtils.assertTemplateGetters;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;

import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.DriverManifest.ModuleManifest;
import de.ims.icarus.model.api.manifest.DriverManifest.ModuleSpec;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.IndexManifest.Coverage;
import de.ims.icarus.model.api.manifest.IndexManifest.Relation;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.DriverManifestImpl;
import de.ims.icarus.model.standard.manifest.DriverManifestImpl.ModuleManifestImpl;
import de.ims.icarus.model.standard.manifest.DriverManifestImpl.ModuleSpecImpl;
import de.ims.icarus.model.standard.manifest.IndexManifestImpl;

/**
 * @author Markus Gärtner
 * @version $Id: DriverManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class DriverManifestImplTest extends ManifestTestCase<DriverManifestImpl> {

	private static final String MODULE_SPEC_ID_1 = "moduleSpec1"; //$NON-NLS-1$
	private static final String MODULE_SPEC_ID_2 = "moduleSpec2"; //$NON-NLS-1$
	private static final String MODULE_SPEC_ID_3 = "moduleSpec3"; //$NON-NLS-1$

	private static final String MODULE_ID_1 = "module1"; //$NON-NLS-1$
	private static final String MODULE_ID_2 = "module2"; //$NON-NLS-1$
	private static final String MODULE_ID_3 = "module3"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected DriverManifestImpl newInstance() {
		return new DriverManifestImpl(location, registry, null);
	}

	private void fillLocationType(DriverManifestImpl manifest) {
		manifest.setLocationType(LocationType.FILE);
	}

	private void fillIndices(DriverManifestImpl manifest) {
		IndexManifestImpl indexManifest1 = new IndexManifestImpl(manifest);
		indexManifest1.setCoverage(Coverage.PARTIAL);
		indexManifest1.setRelation(Relation.ONE_TO_MANY);
		indexManifest1.setSourceLayerId("source-layer-1"); //$NON-NLS-1$
		indexManifest1.setTargetLayerId("target-layer-1"); //$NON-NLS-1$

		manifest.addIndexManifest(indexManifest1);

		IndexManifestImpl indexManifest2 = new IndexManifestImpl(manifest);
		indexManifest2.setCoverage(Coverage.TOTAL_MONOTONIC);
		indexManifest2.setRelation(Relation.ONE_TO_ONE);
		indexManifest2.setSourceLayerId("source-layer-2"); //$NON-NLS-1$
		indexManifest2.setTargetLayerId("target-layer-2"); //$NON-NLS-1$

		manifest.addIndexManifest(indexManifest2);
	}

	private void fillModuleSpecs(DriverManifestImpl manifest) {
		ModuleSpecImpl spec1 = new ModuleSpecImpl(manifest);
		spec1.setId(MODULE_SPEC_ID_1);

		manifest.addModuleSpec(spec1);

		ModuleSpecImpl spec2 = new ModuleSpecImpl(manifest);
		spec2.setId(MODULE_SPEC_ID_2);

		manifest.addModuleSpec(spec2);

		ModuleSpecImpl spec3 = new ModuleSpecImpl(manifest);
		spec3.setId(MODULE_SPEC_ID_3);

		manifest.addModuleSpec(spec3);
	}

	private void fillModuleManifests(DriverManifestImpl manifest) {
		ModuleManifestImpl module1 = new ModuleManifestImpl(location, registry, manifest);
		module1.setId(MODULE_ID_1);
		module1.setModuleSpecId(MODULE_SPEC_ID_1);

		manifest.addModuleManifest(module1);

		ModuleManifestImpl module2 = new ModuleManifestImpl(location, registry, manifest);
		module2.setId(MODULE_ID_2);
		module2.setModuleSpecId(MODULE_SPEC_ID_2);

		manifest.addModuleManifest(module2);

		ModuleManifestImpl module3 = new ModuleManifestImpl(location, registry, manifest);
		module3.setId(MODULE_ID_3);
		module3.setModuleSpecId(MODULE_SPEC_ID_3);

		manifest.addModuleManifest(module3);
	}

	private void fillAll(DriverManifestImpl manifest) {
		fillIdentity(manifest);
		fillLocationType(manifest);
		fillIndices(manifest);
		fillModuleSpecs(manifest);
		fillModuleManifests(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getContextManifest());
	}

	@Test
	public void testEquals() throws Exception {

		DriverManifestImpl other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testMissingEnvironment() throws Exception {
		ManifestLocation manifestLocation = mock(ManifestLocation.class);
		when(manifestLocation.isTemplate()).thenReturn(false);
		CorpusRegistry registry = mock(CorpusRegistry.class);

		thrown.expect(ModelException.class);
		new DriverManifestImpl(manifestLocation, registry, null);
	}

	// MODIFICATION TESTS

	@Test
	public void testSetLocationType() throws Exception {
		for(LocationType locationType : LocationType.values()) {
			manifest.setLocationType(locationType);
			assertSame(locationType, manifest.getLocationType());
		}
	}

	@Test
	public void testSetLocationTypeNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setLocationType(null);
	}

	@Test
	public void testAddIndex() throws Exception {
		IndexManifest indexManifest1 = mock(IndexManifest.class);
		IndexManifest indexManifest2 = mock(IndexManifest.class);
		IndexManifest indexManifest3 = mock(IndexManifest.class);

		manifest.addIndexManifest(indexManifest1);
		manifest.addIndexManifest(indexManifest2);
		manifest.addIndexManifest(indexManifest3);

		List<IndexManifest> list = manifest.getIndexManifests();

		assertEquals(3, list.size());

		assertSame(indexManifest1, list.get(0));
		assertSame(indexManifest2, list.get(1));
		assertSame(indexManifest3, list.get(2));
	}

	@Test
	public void testAddIndexNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addIndexManifest(null);
	}

	@Test
	public void testRemoveIndex() throws Exception {
		IndexManifest indexManifest1 = mock(IndexManifest.class);
		IndexManifest indexManifest2 = mock(IndexManifest.class);
		IndexManifest indexManifest3 = mock(IndexManifest.class);

		manifest.addIndexManifest(indexManifest1);
		manifest.addIndexManifest(indexManifest2);
		manifest.addIndexManifest(indexManifest3);

		manifest.removeIndexManifest(indexManifest2);

		List<IndexManifest> list = manifest.getIndexManifests();

		assertEquals(2, list.size());

		assertSame(indexManifest1, list.get(0));
		assertSame(indexManifest3, list.get(1));

		assertFalse(list.contains(indexManifest2));
	}

	@Test
	public void testRemoveIndexNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removeIndexManifest(null);
	}

	@Test
	public void testRemoveIndexUnknown() throws Exception {
		IndexManifest indexManifest1 = mock(IndexManifest.class);

		thrown.expect(IllegalArgumentException.class);
		manifest.removeIndexManifest(indexManifest1);
	}

	@Test
	public void testAddSpec() throws Exception {
		ModuleSpec spec1 = mock(ModuleSpec.class);
		ModuleSpec spec2 = mock(ModuleSpec.class);
		ModuleSpec spec3 = mock(ModuleSpec.class);

		manifest.addModuleSpec(spec1);
		manifest.addModuleSpec(spec2);
		manifest.addModuleSpec(spec3);

		List<ModuleSpec> list = manifest.getModuleSpecs();

		assertEquals(3, list.size());

		assertSame(spec1, list.get(0));
		assertSame(spec2, list.get(1));
		assertSame(spec3, list.get(2));
	}

	@Test
	public void testAddSpecNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addModuleSpec(null);
	}

	@Test
	public void testRemoveSpec() throws Exception {
		ModuleSpec spec1 = mock(ModuleSpec.class);
		ModuleSpec spec2 = mock(ModuleSpec.class);
		ModuleSpec spec3 = mock(ModuleSpec.class);

		manifest.addModuleSpec(spec1);
		manifest.addModuleSpec(spec2);
		manifest.addModuleSpec(spec3);

		manifest.removeModuleSpec(spec2);

		List<ModuleSpec> list = manifest.getModuleSpecs();

		assertEquals(2, list.size());

		assertSame(spec1, list.get(0));
		assertSame(spec3, list.get(1));

		assertFalse(list.contains(spec2));
	}

	@Test
	public void testRemoveSpecNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removeModuleSpec(null);
	}

	@Test
	public void testRemoveSpecUnknown() throws Exception {
		ModuleSpec spec = mock(ModuleSpec.class);

		thrown.expect(IllegalArgumentException.class);
		manifest.removeModuleSpec(spec);
	}

	@Test
	public void testAddModule() throws Exception {
		ModuleManifest module1 = mock(ModuleManifest.class);
		ModuleManifest module2 = mock(ModuleManifest.class);
		ModuleManifest module3 = mock(ModuleManifest.class);

		manifest.addModuleManifest(module1);
		manifest.addModuleManifest(module2);
		manifest.addModuleManifest(module3);

		List<ModuleManifest> list = manifest.getModuleManifests();

		assertEquals(3, list.size());

		assertSame(module1, list.get(0));
		assertSame(module2, list.get(1));
		assertSame(module3, list.get(2));
	}

	@Test
	public void testAddModuleNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addModuleManifest(null);
	}

	@Test
	public void testRemoveModule() throws Exception {
		ModuleManifest module1 = mock(ModuleManifest.class);
		ModuleManifest module2 = mock(ModuleManifest.class);
		ModuleManifest module3 = mock(ModuleManifest.class);

		manifest.addModuleManifest(module1);
		manifest.addModuleManifest(module2);
		manifest.addModuleManifest(module3);

		manifest.removeModuleManifest(module2);

		List<ModuleManifest> list = manifest.getModuleManifests();

		assertEquals(2, list.size());

		assertSame(module1, list.get(0));
		assertSame(module3, list.get(1));

		assertFalse(list.contains(module2));
	}

	@Test
	public void testRemoveModuleNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removeModuleSpec(null);
	}

	@Test
	public void testRemoveModuleUnknown() throws Exception {
		ModuleManifest module = mock(ModuleManifest.class);

		thrown.expect(IllegalArgumentException.class);
		manifest.removeModuleManifest(module);
	}

	@Test
	public void testLookupModule() throws Exception {
		ModuleManifest module1 = mock(ModuleManifest.class);
		when(module1.getId()).thenReturn(MODULE_ID_1);
		ModuleManifest module2 = mock(ModuleManifest.class);
		when(module2.getId()).thenReturn(MODULE_ID_2);
		ModuleManifest module3 = mock(ModuleManifest.class);
		when(module3.getId()).thenReturn(MODULE_ID_3);

		manifest.addModuleManifest(module1);
		manifest.addModuleManifest(module2);
		manifest.addModuleManifest(module3);

		assertSame(module1, manifest.getModuleManifest(MODULE_ID_1));
		assertSame(module2, manifest.getModuleManifest(MODULE_ID_2));
		assertSame(module3, manifest.getModuleManifest(MODULE_ID_3));
	}

	@Test
	public void testLookupModuleNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.getModuleManifest(null);
	}

	@Test
	public void testLookupModuleUnknown() throws Exception {
		assertNull(manifest.getModuleManifest(MODULE_ID_1));
	}

	@Test
	public void testLookupSpec() throws Exception {
		ModuleSpec spec1 = mock(ModuleSpec.class);
		when(spec1.getId()).thenReturn(MODULE_SPEC_ID_1);
		ModuleSpec spec2 = mock(ModuleSpec.class);
		when(spec2.getId()).thenReturn(MODULE_SPEC_ID_2);
		ModuleSpec spec3 = mock(ModuleSpec.class);
		when(spec3.getId()).thenReturn(MODULE_SPEC_ID_3);

		manifest.addModuleSpec(spec1);
		manifest.addModuleSpec(spec2);
		manifest.addModuleSpec(spec3);

		assertSame(spec1, manifest.getModuleSpec(MODULE_SPEC_ID_1));
		assertSame(spec2, manifest.getModuleSpec(MODULE_SPEC_ID_2));
		assertSame(spec3, manifest.getModuleSpec(MODULE_SPEC_ID_3));
	}

	@Test
	public void testLookupSpecNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.getModuleSpec(null);
	}

	@Test
	public void testLookupSpecUnknown() throws Exception {
		assertNull(manifest.getModuleSpec(MODULE_SPEC_ID_1));
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		DriverManifestImpl template = newInstance();
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(DriverManifest.class, manifest, template);
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
	public void testXmlLocationType() throws Exception {

		fillIdentity(manifest);

		for(LocationType locationType : LocationType.values()) {
			manifest.setLocationType(locationType);
			assertSerializationEquals("Location type "+locationType, manifest, newInstance()); //$NON-NLS-1$
		}
	}

	@Test
	public void testXmlIndices() throws Exception {

		fillIdentity(manifest);
		fillIndices(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlModuleSpecs() throws Exception {

		fillIdentity(manifest);
		fillModuleSpecs(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlModules() throws Exception {

		fillIdentity(manifest);
		fillModuleManifests(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, newInstance());
	}
}
