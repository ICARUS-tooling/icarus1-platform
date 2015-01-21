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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/AnnotationLayerManifestImplTest.java $
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.standard.manifest.AnnotationLayerManifestImpl;
import de.ims.icarus.model.standard.manifest.AnnotationManifestImpl;
import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.types.ValueType;

/**
 * @author Markus Gärtner
 * @version $Id: AnnotationLayerManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class AnnotationLayerManifestImplTest extends ManifestTestCase<AnnotationLayerManifestImpl> {

	private static final String TEST_DEFAULT_KEY = "defaultAnnotationKey"; //$NON-NLS-1$

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected AnnotationLayerManifestImpl newInstance() {
		return new AnnotationLayerManifestImpl(location, registry, null);
	}

	private void fillDeepAnnotation(AnnotationLayerManifestImpl manifest) {
		manifest.setDeepAnnotation(!AnnotationLayerManifest.DEFAULT_DEEP_ANNOTATION_VALUE);
	}

	private void fillAllowUnknownKeys(AnnotationLayerManifestImpl manifest) {
		manifest.setAllowUnknownKeys(!AnnotationLayerManifest.DEFAULT_ALLOW_UNKNOWN_KEYS_VALUE);
	}

	private void fillIndexable(AnnotationLayerManifestImpl manifest) {
		manifest.setIndexable(!AnnotationLayerManifest.DEFAULT_INDEXABLE_VALUE);
	}

	private void fillSearchable(AnnotationLayerManifestImpl manifest) {
		manifest.setSearchable(!AnnotationLayerManifest.DEFAULT_SEARCHABLE_VALUE);
	}

	private void fillDefaultKey(AnnotationLayerManifestImpl manifest) {
		manifest.setDefaultKey(TEST_DEFAULT_KEY);
	}

	private void fillAnnotationManifest(AnnotationLayerManifestImpl manifest) {
		AnnotationManifestImpl annotationManifest = new AnnotationManifestImpl(location, registry);
		annotationManifest.setKey(TEST_DEFAULT_KEY);
		annotationManifest.setValueType(ValueType.INTEGER);
		annotationManifest.setSupportedRange(new ValueRangeImpl(ValueType.INTEGER, 1, 3456));

		manifest.addAnnotationManifest(annotationManifest);

		annotationManifest = new AnnotationManifestImpl(location, registry);
		annotationManifest.setKey("someOtherAnnotation"); //$NON-NLS-1$
		annotationManifest.setValueType(ValueType.STRING);

		manifest.addAnnotationManifest(annotationManifest);
	}

	private void fillAll(AnnotationLayerManifestImpl manifest) {
		fillIdentity(manifest);
		fillAllowUnknownKeys(manifest);
		fillDeepAnnotation(manifest);
		fillDefaultKey(manifest);
		fillIndexable(manifest);
		fillSearchable(manifest);
		fillAnnotationManifest(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();

		assertNull(manifest.getGroupManifest());
	}

	@Test
	public void testEquals() throws Exception {

		AnnotationLayerManifestImpl other = newInstance();

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
	public void testAddAnnotationManifest() throws Exception {
		AnnotationManifest annotationManifest1 = mock(AnnotationManifest.class);
		when(annotationManifest1.getKey()).thenReturn("key1"); //$NON-NLS-1$
		AnnotationManifest annotationManifest2 = mock(AnnotationManifest.class);
		when(annotationManifest2.getKey()).thenReturn("key2"); //$NON-NLS-1$
		AnnotationManifest annotationManifest3 = mock(AnnotationManifest.class);
		when(annotationManifest3.getKey()).thenReturn("key3"); //$NON-NLS-1$

		manifest.addAnnotationManifest(annotationManifest1);
		manifest.addAnnotationManifest(annotationManifest2);
		manifest.addAnnotationManifest(annotationManifest3);

		Set<String> keys = manifest.getAvailableKeys();

		assertEquals(3, keys.size());

		assertTrue(keys.contains(annotationManifest1.getKey()));
		assertTrue(keys.contains(annotationManifest2.getKey()));
		assertTrue(keys.contains(annotationManifest3.getKey()));

		assertSame(annotationManifest1, manifest.getAnnotationManifest(annotationManifest1.getKey()));
		assertSame(annotationManifest2, manifest.getAnnotationManifest(annotationManifest2.getKey()));
		assertSame(annotationManifest3, manifest.getAnnotationManifest(annotationManifest3.getKey()));
	}

	@Test
	public void testAddAnnotationManifestDuplicate() throws Exception {
		AnnotationManifest annotationManifest1 = mock(AnnotationManifest.class);
		when(annotationManifest1.getKey()).thenReturn("key1"); //$NON-NLS-1$

		manifest.addAnnotationManifest(annotationManifest1);

		thrown.expect(IllegalArgumentException.class);
		manifest.addAnnotationManifest(annotationManifest1);
	}

	@Test
	public void testAddAnnotationManifestNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addAnnotationManifest(null);
	}

	@Test
	public void testRemoveAnnotationManifest() throws Exception {
		AnnotationManifest annotationManifest1 = mock(AnnotationManifest.class);
		when(annotationManifest1.getKey()).thenReturn("key1"); //$NON-NLS-1$
		AnnotationManifest annotationManifest2 = mock(AnnotationManifest.class);
		when(annotationManifest2.getKey()).thenReturn("key2"); //$NON-NLS-1$
		AnnotationManifest annotationManifest3 = mock(AnnotationManifest.class);
		when(annotationManifest3.getKey()).thenReturn("key3"); //$NON-NLS-1$

		manifest.addAnnotationManifest(annotationManifest1);
		manifest.addAnnotationManifest(annotationManifest2);
		manifest.addAnnotationManifest(annotationManifest3);

		manifest.removeAnnotationManifest(annotationManifest2);

		Set<String> keys = manifest.getAvailableKeys();

		assertEquals(2, keys.size());

		assertTrue(keys.contains(annotationManifest1.getKey()));
		assertFalse(keys.contains(annotationManifest2.getKey()));
		assertTrue(keys.contains(annotationManifest3.getKey()));

		assertSame(annotationManifest1, manifest.getAnnotationManifest(annotationManifest1.getKey()));
		assertSame(annotationManifest3, manifest.getAnnotationManifest(annotationManifest3.getKey()));

		thrown.expect(IllegalArgumentException.class);
		manifest.getAnnotationManifest(annotationManifest2.getKey());
	}

	@Test
	public void testRemoveAnnotationManifestNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removeAnnotationManifest(null);
	}

	@Test
	public void testGetAnnotationManifestNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.getAnnotationManifest(null);
	}

	@Test
	public void testGetAnnotationManifestUnknown() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		manifest.getAnnotationManifest("unknownKey"); //$NON-NLS-1$
	}

	@Test
	public void testSetDeepAnnotation() throws Exception {
		manifest.setDeepAnnotation(true);
		assertTrue(manifest.isDeepAnnotation());

		manifest.setDeepAnnotation(false);
		assertFalse(manifest.isDeepAnnotation());
	}

	@Test
	public void testSetAllowUnknownKeys() throws Exception {
		manifest.setAllowUnknownKeys(true);
		assertTrue(manifest.isAllowUnknownKeys());

		manifest.setAllowUnknownKeys(false);
		assertFalse(manifest.isAllowUnknownKeys());
	}

	@Test
	public void testSetIndexable() throws Exception {
		manifest.setIndexable(true);
		assertTrue(manifest.isIndexable());

		manifest.setIndexable(false);
		assertFalse(manifest.isIndexable());
	}

	@Test
	public void testSetSearchable() throws Exception {
		manifest.setSearchable(true);
		assertTrue(manifest.isSearchable());

		manifest.setSearchable(false);
		assertFalse(manifest.isSearchable());
	}

	// TEMPLATE TESTS

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		AnnotationLayerManifestImpl template = newInstance();
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(AnnotationLayerManifest.class, manifest, template);
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
	public void testXmlDeepAnnotation() throws Exception {

		fillIdentity(manifest);
		fillDeepAnnotation(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlAllowUnknownKeys() throws Exception {

		fillIdentity(manifest);
		fillAllowUnknownKeys(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlSearchable() throws Exception {

		fillIdentity(manifest);
		fillSearchable(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlIndexable() throws Exception {

		fillIdentity(manifest);
		fillIndexable(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlDefaultKey() throws Exception {

		fillIdentity(manifest);
		fillDefaultKey(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlAnnotationManifest() throws Exception {

		fillIdentity(manifest);
		fillAnnotationManifest(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, newInstance());
	}
}
