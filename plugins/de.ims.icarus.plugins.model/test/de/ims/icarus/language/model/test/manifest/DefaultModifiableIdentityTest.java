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
import static de.ims.icarus.language.model.test.TestUtils.assertObjectContract;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultModifiableIdentityTest implements ManifestTestConstants {

	private DefaultModifiableIdentity identity;

	@Rule
	public final ExpectedException thrown= ExpectedException.none();

	@Before
	public void prepare() {
		identity = new DefaultModifiableIdentity();
	}

	@Test
	public void testGeneral() throws Exception {
		assertObjectContract(identity);
	}

	@Test
	public void testOwner() throws Exception {
		assertNotNull(identity.getOwner());
	}

	@Test
	public void testSetInvalidIdNull() throws Exception {
		thrown.expect(NullPointerException.class);
		identity.setId(INVALID_ID_NULL);
	}

	@Test
	public void testSetInvalidIdContent() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		identity.setId(INVALID_ID_CONTENT);
	}

	@Test
	public void testSetInvalidIdLength() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		identity.setId(INVALID_ID_LENGTH);
	}

	@Test
	public void testSetInvalidIdBegin() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		identity.setId(INVALID_ID_BEGIN);
	}

	@Test
	public void testSetId() throws Exception {
		identity.setId(TEST_ID);

		assertSame(TEST_ID, identity.getId());
	}

	@Test
	public void testSetName() throws Exception {
		identity.setName(null);
		identity.setName(TEST_NAME);

		assertSame(TEST_NAME, identity.getName());
	}

	@Test
	public void testSetDescription() throws Exception {
		identity.setDescription(null);
		identity.setDescription(TEST_DESCRIPTION);

		assertSame(TEST_DESCRIPTION, identity.getDescription());
	}

	@Test
	public void testSetIcon() throws Exception {
		identity.setIcon(null);
		identity.setIcon(TEST_ICON);

		assertSame(TEST_ICON, identity.getIcon());
	}

	@Test
	public void testEqualsSame() throws Exception {
		assertHashEquals(identity, identity);
	}

	@Test
	public void testEqualsOther() throws Exception {
		assertHashEquals(identity, new DefaultModifiableIdentity(TEST_ID, null));
	}
}
