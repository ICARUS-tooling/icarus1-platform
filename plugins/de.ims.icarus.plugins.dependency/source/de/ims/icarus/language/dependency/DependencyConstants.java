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
package de.ims.icarus.language.dependency;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DependencyConstants {

	public static final String GRAMMAR_ID = "dependency"; //$NON-NLS-1$

	public static final String CONTENT_TYPE_ID = "DependencyDataContentType"; //$NON-NLS-1$


	// mask fields for data events
	public static final int DATA_FIELD_FORM = (1 << 0);
	public static final int DATA_FIELD_LEMMA = (1 << 1);
	public static final int DATA_FIELD_FEATURES = (1 << 2);
	public static final int DATA_FIELD_POS = (1 << 3);
	public static final int DATA_FIELD_HEAD = (1 << 4);
	public static final int DATA_FIELD_RELATION = (1 << 5);
	public static final int DATA_FIELD_INDEX = (1 << 6);
	public static final int DATA_FIELD_FLAG = (1 << 7);

	/**
	 * Level value to signal that no validation has been performed yet.
	 */
	public static final int DATA_LEVEL_UNDEFINED = -1;

	public static final int TABLE_INDEX_INDEX = 0;
	public static final int TABLE_INDEX_FORM = 1;
	public static final int TABLE_INDEX_LEMMA = 2;
	public static final int TABLE_INDEX_FEATURES = 3;
	public static final int TABLE_INDEX_POS = 4;
	public static final int TABLE_INDEX_HEAD = 5;
	public static final int TABLE_INDEX_REL = 6;

	// constraint key labels
	public static final String RELATION_KEY = "rel"; //$NON-NLS-1$

	// constraint order
	public static final int GROUP_ANY = 0;
	public static final int GROUP_ROOT = 1;
	public static final int GROUP_FORM = 2;
	public static final int GROUP_LEMMA = 3;
	public static final int GROUP_FEATURES = 4;
	public static final int GROUP_POS = 5;
	public static final int GROUP_EXISTENCE = 6;
	public static final int GROUP_RELATION = 7;
	public static final int GROUP_DIRECTION = 8;
	public static final int GROUP_DISTANCE = 9;

	// highlight flags and mask fields
	public static final int HIGHLIGHT_NONE = 0;
	public static final int HIGHLIGHT_GENERAL = 1;
	public static final int HIGHLIGHT_GROUP = (1 << 2);
	public static final int HIGHLIGHT_NODE_GENERAL = (1 << 3) | HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_EDGE_GENERAL = (1 << 4) | HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_ROOT = (1 << 8) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_ROOT_GROUP = (1 << 9) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_FORM = (1 << 10) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_FORM_GROUP = (1 << 11) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_LEMMA = (1 << 12) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_LEMMA_GROUP = (1 << 13) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_FEATURES = (1 << 14) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_FEATURES_GROUP = (1 << 15) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_POS = (1 << 16) | HIGHLIGHT_NODE_GENERAL;
	public static final int HIGHLIGHT_POS_GROUP = (1 << 17) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_EXISTENCE = (1 << 18) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_EXISTENCE_GROUP = (1 << 19) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_RELATION = (1 << 20) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_RELATION_GROUP = (1 << 21) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_DISTANCE = (1 << 22) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_DISTANCE_GROUP = (1 << 23) | HIGHLIGHT_GROUP;
	public static final int HIGHLIGHT_DIRECTION = (1 << 24) | HIGHLIGHT_EDGE_GENERAL;
	public static final int HIGHLIGHT_DIRECTION_GROUP = (1 << 25) | HIGHLIGHT_GROUP;

	public static final int HIGHLIGHT_EDGE_MASK =
		(HIGHLIGHT_RELATION | HIGHLIGHT_DIRECTION | HIGHLIGHT_DISTANCE) & ~HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_EDGE_GROUP_MASK =
		(HIGHLIGHT_EXISTENCE_GROUP | HIGHLIGHT_RELATION_GROUP | HIGHLIGHT_DIRECTION_GROUP | HIGHLIGHT_DISTANCE_GROUP) & ~HIGHLIGHT_GROUP;

	public static final int HIGHLIGHT_NODE_MASK =
		(HIGHLIGHT_FORM | HIGHLIGHT_POS | HIGHLIGHT_LEMMA | HIGHLIGHT_FEATURES) & ~HIGHLIGHT_GENERAL;
	public static final int HIGHLIGHT_NODE_GROUP_MASK =
		(HIGHLIGHT_FORM_GROUP | HIGHLIGHT_POS_GROUP | HIGHLIGHT_LEMMA_GROUP | HIGHLIGHT_FEATURES_GROUP) & ~HIGHLIGHT_GROUP;

}
