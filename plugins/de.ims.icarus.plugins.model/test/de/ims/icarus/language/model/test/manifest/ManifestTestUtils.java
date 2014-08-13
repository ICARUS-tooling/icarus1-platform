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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import de.ims.icarus.model.api.manifest.ModifiableIdentity;
import de.ims.icarus.model.standard.manifest.AbstractManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestTestUtils implements ManifestTestConstants {

	public static void assertIdSetterSpec(AbstractManifest<?> manifest) {

		assertSetLegalId(manifest);
		assertSetInvalidIdBegin(manifest);
		assertSetInvalidIdContent(manifest);
		assertSetInvalidIdLength(manifest);
		assertSetInvalidIdNull(manifest);
	}

	public static void assertSetLegalId(AbstractManifest<?> manifest) {
		manifest.setId(LEGAL_ID);
	}

	public static void assertSetInvalidIdLength(AbstractManifest<?> manifest) {
		try {
			manifest.setId(INVALID_ID_LENGTH);
			fail("Expected IllegalArgumentException for id of invalid length"); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdBegin(AbstractManifest<?> manifest) {
		try {
			manifest.setId(INVALID_ID_BEGIN);
			fail("Expected IllegalArgumentException for id of invalid begin"); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdContent(AbstractManifest<?> manifest) {
		try {
			manifest.setId(INVALID_ID_CONTENT);
			fail("Expected IllegalArgumentException for id of invalid content"); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdNull(AbstractManifest<?> manifest) {
		try {
			manifest.setId(INVALID_ID_NULL);
			fail("Expected NullPointerException for null id"); //$NON-NLS-1$
		} catch(NullPointerException e) {
			// no-op
		}
	}

	public static void assertIdentitySetters(ModifiableIdentity identity) {

		identity.setId(LEGAL_ID);

		assertSetInvalidIdNull(identity);
		assertSetInvalidIdBegin(identity);
		assertSetInvalidIdLength(identity);
		assertSetInvalidIdContent(identity);

		identity.setName(TEST_NAME);
		assertSame(TEST_NAME, identity.getName());

		identity.setDescription(TEST_DESCRIPTION);
		assertSame(TEST_DESCRIPTION, identity.getDescription());

		identity.setIcon(TEST_ICON);
		assertSame(TEST_ICON, identity.getIcon());
	}

	public static void assertSetInvalidId(ModifiableIdentity identity) {
		try {
			identity.setId(null);
			fail("Expected NullPointerException for null id"); //$NON-NLS-1$
		} catch(NullPointerException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdNull(ModifiableIdentity identity) {
		try {
			identity.setId(null);
			fail("Expected NullPointerException for null id"); //$NON-NLS-1$
		} catch(NullPointerException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdLength(ModifiableIdentity identity) {
		try {
			identity.setId(INVALID_ID_LENGTH);
			fail("Expected IllegalArgumentException for id with invalid length: "+INVALID_ID_LENGTH); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdContent(ModifiableIdentity identity) {
		try {
			identity.setId(INVALID_ID_CONTENT);
			fail("Expected IllegalArgumentException for id with invalid content: "+INVALID_ID_CONTENT); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIdBegin(ModifiableIdentity identity) {
		try {
			identity.setId(INVALID_ID_BEGIN);
			fail("Expected IllegalArgumentException for id with invalid begin: "+INVALID_ID_BEGIN); //$NON-NLS-1$
		} catch(IllegalArgumentException e) {
			// no-op
		}
	}

	public static void assertSetInvalidName(ModifiableIdentity identity) {
		try {
			identity.setName(null);
			fail("Expected NullPointerException for null name"); //$NON-NLS-1$
		} catch(NullPointerException e) {
			// no-op
		}
	}

	public static void assertSetInvalidDescription(ModifiableIdentity identity) {
		try {
			identity.setDescription(null);
			fail("Expected NullPointerException for null description"); //$NON-NLS-1$
		} catch(NullPointerException e) {
			// no-op
		}
	}

	public static void assertSetInvalidIcon(ModifiableIdentity identity) {
		try {
			identity.setIcon(null);;
			fail("Expected NullPointerException for null icon"); //$NON-NLS-1$
		} catch(NullPointerException e) {
			// no-op
		}
	}

	public static <M extends AbstractManifest<?>> M mockManifest(Class<M> manifestClass) {
		M manifest = mock(manifestClass);

		manifest.setId(manifestClass.getName());

		return manifest;
	}
}
