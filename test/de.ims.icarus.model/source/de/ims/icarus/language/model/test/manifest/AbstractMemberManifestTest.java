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
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/AbstractMemberManifestTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertHashEquals;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdSetterSpec;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationNotEquals;

import org.junit.Test;

import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.AbstractMemberManifest;
import de.ims.icarus.model.standard.manifest.DocumentationImpl;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl.OptionImpl;
import de.ims.icarus.model.types.ValueType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractMemberManifestTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class AbstractMemberManifestTest extends ManifestTestCase<AbstractMemberManifestTest.DummyManifest> {

	public static class DummyManifest extends AbstractMemberManifest<MemberManifest> {

		/**
		 * @param manifestLocation
		 * @param registry
		 */
		public DummyManifest(ManifestLocation manifestLocation,
				CorpusRegistry registry) {
			super(manifestLocation, registry);
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

	private void fillDocumentation(DummyManifest manifest) {
		manifest.setDocumentation(new DocumentationImpl("this is some example documentation")); //$NON-NLS-1$
	}

	private void fillProperties(DummyManifest manifest) {
		manifest.setProperty("test1", "someProperty"); //$NON-NLS-1$ //$NON-NLS-2$

		manifest.setProperty("test2", CollectionUtils.asList("value1", "value2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		manifest.setProperty("test3", 12345); //$NON-NLS-1$
	}

	private void fillOptions(DummyManifest manifest) {
		OptionsManifestImpl optionsManifest = new OptionsManifestImpl(location, registry);
		optionsManifest.addOption(new OptionImpl("test1", ValueType.STRING)); //$NON-NLS-1$
		optionsManifest.addOption(new OptionImpl("test2", ValueType.STRING).setMultiValue(true)); //$NON-NLS-1$
		optionsManifest.addOption(new OptionImpl("test3", ValueType.INTEGER)); //$NON-NLS-1$

		manifest.setOptionsManifest(optionsManifest);
	}

	private void fillAll(DummyManifest manifest) {
		fillIdentity(manifest);
		fillOptions(manifest);
		fillProperties(manifest);
		fillDocumentation(manifest);
	}

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected DummyManifest newInstance() {
		return new DummyManifest(location, registry);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();
	}

	@Test
	public void testEquals() throws Exception {
		DummyManifest other = newInstance();

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testIdentitySetters() throws Exception {
		assertIdSetterSpec(manifest);
	}

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
	public void testXmlDocumentation() throws Exception {

		fillIdentity(manifest);
		fillDocumentation(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlOptions() throws Exception {

		fillIdentity(manifest);
		fillOptions(manifest);

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlPropertiesWithTypeLoss() throws Exception {

		fillIdentity(manifest);

		manifest.setProperty("test1", 1234); //$NON-NLS-1$

		assertSerializationNotEquals(manifest, newInstance());
	}

	@Test
	public void testXmlPropertiesWithCollectionLoss() throws Exception {

		fillIdentity(manifest);

		manifest.setProperty("test1", CollectionUtils.asList("value1", "value2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		assertSerializationNotEquals(manifest, newInstance());
	}

	@Test
	public void testXmlProperties() throws Exception {

		fillIdentity(manifest);

		manifest.setProperty("test1", "value1"); //$NON-NLS-1$ //$NON-NLS-2$
		manifest.setProperty("test2", "value2"); //$NON-NLS-1$ //$NON-NLS-2$

		assertSerializationEquals(manifest, newInstance());
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest, newInstance());
	}
}
