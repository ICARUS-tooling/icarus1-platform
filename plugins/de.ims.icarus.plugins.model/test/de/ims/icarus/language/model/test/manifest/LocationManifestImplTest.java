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

import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.api.manifest.LocationManifest.PathEntry;
import de.ims.icarus.model.api.manifest.LocationManifest.PathType;
import de.ims.icarus.model.api.manifest.PathResolverManifest;
import de.ims.icarus.model.standard.manifest.LocationManifestImpl;
import de.ims.icarus.model.standard.manifest.LocationManifestImpl.PathEntryImpl;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationManifestImplTest implements ManifestTestConstants {

	private LocationManifestImpl manifest;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	private void fillPath(LocationManifestImpl manifest) {
		manifest.setPath(TEST_PATH);
	}

	private void fillPathEntry(LocationManifestImpl manifest) {
		manifest.addPathEntry(new PathEntryImpl(PathType.FILE, TEST_PATH));
		manifest.addPathEntry(new PathEntryImpl(PathType.FOLDER, TEST_PATH));
	}

	private void fillAll(LocationManifestImpl manifest) {
		fillPath(manifest);
		fillPathEntry(manifest);
	}

	@Before
	public void prepare() {
		manifest = new LocationManifestImpl();
	}

	@Test
	public void testSetPath() throws Exception {
		manifest.setPath(TEST_PATH);
	}

	@Test
	public void testSetPathNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.setPath(null);
	}

	@Test
	public void testSetPathEmpty() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		manifest.setPath(""); //$NON-NLS-1$
	}

	@Test
	public void testSetPathResolverManifest() throws Exception {
		final PathResolverManifest pathResolverManifest = mock(PathResolverManifest.class);

		manifest.setPathResolverManifest(pathResolverManifest);

		assertSame(pathResolverManifest, manifest.getPathResolverManifest());
	}

	@Test
	public void testAddPathEntry() throws Exception {
		PathEntry entry1 = mock(PathEntry.class);
		PathEntry entry2 = mock(PathEntry.class);
		PathEntry entry3 = mock(PathEntry.class);

		manifest.addPathEntry(entry1);
		manifest.addPathEntry(entry2);
		manifest.addPathEntry(entry3);

		List<PathEntry> pathEntries = manifest.getPathEntries();

		assertEquals(3, pathEntries.size());

		assertSame(entry1, pathEntries.get(0));
		assertSame(entry2, pathEntries.get(1));
		assertSame(entry3, pathEntries.get(2));
	}

	@Test
	public void testAddPathEntryNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addPathEntry(null);
	}

	@Test
	public void testRemovePathEntry() throws Exception {
		PathEntry entry1 = mock(PathEntry.class);
		PathEntry entry2 = mock(PathEntry.class);
		PathEntry entry3 = mock(PathEntry.class);

		manifest.addPathEntry(entry1);
		manifest.addPathEntry(entry2);
		manifest.addPathEntry(entry3);

		manifest.removePathEntry(entry2);

		List<PathEntry> pathEntries = manifest.getPathEntries();

		assertEquals(2, pathEntries.size());

		assertSame(entry1, pathEntries.get(0));
		assertSame(entry3, pathEntries.get(1));
	}

	@Test
	public void testRemovePathEntryNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.removePathEntry(null);
	}

	@Test
	public void testXmlEmpty() throws Exception {
		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new LocationManifestImpl());
	}

	@Test
	public void testXmlPath() throws Exception {
		fillPath(manifest);

		assertSerializationEquals(manifest, new LocationManifestImpl());
	}

	@Test
	public void testXmlPathEntry() throws Exception {
		fillPathEntry(manifest);

		thrown.expect(IllegalStateException.class);
		assertSerializationEquals(manifest, new LocationManifestImpl());
	}

	@Test
	public void testXmlFull() throws Exception {
		fillAll(manifest);

		assertSerializationEquals(manifest, new LocationManifestImpl());
	}
}
