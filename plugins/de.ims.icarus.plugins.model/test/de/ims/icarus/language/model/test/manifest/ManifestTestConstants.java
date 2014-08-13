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

import javax.swing.Icon;

import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestLocation.VirtualManifestInputLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.registry.CorpusRegistryImpl;
import de.ims.icarus.model.xml.sax.IconWrapper;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ManifestTestConstants {

	public static final ManifestLocation DEFAULT_TEMPLATE_LOCATION =
			new VirtualManifestInputLocation(ManifestXmlTestUtils.class.getClassLoader(), true);

	public static final ManifestLocation DEFAULT_LIVE_LOCATION =
			new VirtualManifestInputLocation(ManifestXmlTestUtils.class.getClassLoader(), false);

	public static final CorpusRegistry DEFAULT_REGISTRY = new CorpusRegistryImpl();

	public static final String LEGAL_ID = "test-id:someLegelValue012345"; //$NON-NLS-1$
	public static final String INVALID_ID_LENGTH = "id"; //$NON-NLS-1$
	public static final String INVALID_ID_NULL = null;
	public static final String INVALID_ID_CONTENT = "id_with_illegal_characters&%)(/&%"; //$NON-NLS-1$
	public static final String INVALID_ID_BEGIN = "-id_with_illegal_begin_character"; //$NON-NLS-1$

	public static final String TEST_TEMPLATE_ID = "testTemplateId"; //$NON-NLS-1$
	public static final String TEST_ID = "testId"; //$NON-NLS-1$

	public static final String TEST_NAME = "Some Fancy Name"; //$NON-NLS-1$
	public static final String TEST_DESCRIPTION = "This is a description usable for a manifest"; //$NON-NLS-1$
	public static final String TEST_ICON_NAME = "testIconName"; //$NON-NLS-1$
	public static final Icon TEST_ICON = new IconWrapper(TEST_ICON_NAME);
	public static final String TEST_PATH = "path/to/some/fancy/location/"; //$NON-NLS-1$
}
