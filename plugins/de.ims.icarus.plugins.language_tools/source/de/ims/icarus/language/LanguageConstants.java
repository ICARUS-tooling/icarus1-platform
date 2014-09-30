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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.language;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface LanguageConstants {
	// flags
	public static final int FLAG_PROJECTIVE = (1 << 0);

	/**
	 * Head value to mark the root node.
	 */
	public static final short DATA_HEAD_ROOT = -1;

	public static final String DATA_ROOT_LABEL = "<root>"; //$NON-NLS-1$

	public static final String DATA_UNDEFINED_LABEL = "?"; //$NON-NLS-1$

	public static final String DATA_GROUP_LABEL = "<*>"; //$NON-NLS-1$

	public static final String DATA_LEFT_LABEL = "<<"; //$NON-NLS-1$

	public static final String DATA_RIGHT_LABEL = ">>"; //$NON-NLS-1$

	public static final int DATA_LEFT_VALUE = -1;

	public static final int DATA_RIGHT_VALUE = 1;

	public static final int DATA_GROUP_VALUE = -3;

	public static final int DATA_UNDEFINED_VALUE = -2;

	public static final float DATA_UNDEFINED_FLOAT_VALUE = Float.NEGATIVE_INFINITY;

	public static final double DATA_UNDEFINED_DOUBLE_VALUE = Double.NEGATIVE_INFINITY;

	public static final int DATA_YES_VALUE = 0;

	public static final int DATA_NO_VALUE = -1;
}
