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
package de.ims.icarus.model.util;

import de.ims.icarus.model.api.members.Item;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusMemberUtils {

	public static String mismatchMessage(String msg, Item expected, Item provided) {
		return String.format("%s: expected %s - got %s", msg, expected, provided); //$NON-NLS-1$
	}

	public static String sizeMismatchMessage(String msg, int expected, int size) {
		return String.format("%s: expected size %d - got %d", msg, expected, size); //$NON-NLS-1$
	}

	public static String offsetMismatchMessage(String msg, int expected, int index) {
		return String.format("%s: expected offset %d - got %d", msg, expected, index); //$NON-NLS-1$
	}

	public static String illegalOffsetMessage(String msg, int index) {
		return String.format("%s: unexpected offset %d", msg, index); //$NON-NLS-1$
	}

	public static String outOfBoundsMessage(String msg, int index, int min, int max) {
		return String.format("%s: index %d out of bounds [%d,%d]", msg, index, min, max); //$NON-NLS-1$
	}

	public static String insufficientEdgesMessage(String msg, Item node, int required, int count) {
		return String.format("%s: insufficient edge count of %d at node %s - got %d", msg, required, node, count); //$NON-NLS-1$
	}

	public static String edgesOverflowMessage(String msg, Item node, int max, int count) {
		return String.format("%s: edge count %d exceeds limit of %d at node %s", msg, count, max, node); //$NON-NLS-1$
	}
}
