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
package de.ims.icarus.model.api.manifest;

import de.ims.icarus.util.ClassUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestUtils {

	public static boolean equals(ImplementationManifest m1, ImplementationManifest m2) {
		return ClassUtils.equals(m1.getSourceType(), m2.getSourceType())
				&& ClassUtils.equals(m1.getSource(), m2.getSource())
				&& ClassUtils.equals(m1.getClassname(), m2.getClassname())
				&& m1.isUseFactory()==m2.isUseFactory();
	}
}
