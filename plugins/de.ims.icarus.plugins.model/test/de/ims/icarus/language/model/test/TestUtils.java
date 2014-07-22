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
package de.ims.icarus.language.model.test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class TestUtils {

    private static boolean equalsRegardingNull(Object expected, Object actual) {
        if (expected == null) {
            return actual == null;
        }

        return isEquals(expected, actual);
    }

    private static boolean isEquals(Object expected, Object actual) {
        return expected.equals(actual);
    }

    private static boolean isHashEquals(Object expected, Object actual) {
        return expected.hashCode()==actual.hashCode();
    }

	public static void assertHashEquals(Object expected, Object actual) {
		assertHashEquals(null, expected, actual);
	}

	public static void assertHashEquals(String message, Object expected, Object actual) {
		assertNotNull("Expected", expected);
		assertNotNull("Actual", actual);

		if(isEquals(expected, actual) && !isHashEquals(expected, actual)) {

		}
	}
}
