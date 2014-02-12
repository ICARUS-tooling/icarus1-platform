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
package de.ims.icarus.language.model.registry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DefaultLayerTypes {

	public static final String MARK_TOKENS = "mark:tokens"; //$NON-NLS-1$
	public static final String MARK_SENTENCES = "mark:sentences"; //$NON-NLS-1$
	public static final String MARK_DOCUMENTS = "mark:documents"; //$NON-NLS-1$
	public static final String MARK_PARAGRAPHS = "mark:paragraphs"; //$NON-NLS-1$
	public static final String MARK_CHAPTERS = "mark:chapters"; //$NON-NLS-1$
	public static final String MARK_COREFERENCE_CLUSTER = "mark:coreference-cluster"; //$NON-NLS-1$
	public static final String MARK_MENTIONS = "mark:mentions"; //$NON-NLS-1$
	public static final String MARK_PHRASES = "mark:phrases"; //$NON-NLS-1$
	public static final String MARK_LAYER_OVERLAY = "mark:layer-overlay"; //$NON-NLS-1$

	public static final String STRUCT_DEPENDENCY = "struct:dependency"; //$NON-NLS-1$
	public static final String STRUCT_PHRASE = "struct:phrase"; //$NON-NLS-1$
	public static final String STRUCT_LFG = "struct:lfg"; //$NON-NLS-1$
	public static final String STRUCT_COREFERENCE = "struct:coreference"; //$NON-NLS-1$

	public static final String ANNO_FORM = "anno:form"; //$NON-NLS-1$
	public static final String ANNO_POS = "anno:part-of-speech"; //$NON-NLS-1$
	public static final String ANNO_LEMMA = "anno:lemma"; //$NON-NLS-1$
	public static final String ANNO_ROLE = "anno:role"; //$NON-NLS-1$
	public static final String ANNO_RELATION = "anno:relation"; //$NON-NLS-1$
	public static final String ANNO_FEATURES = "anno:features"; //$NON-NLS-1$
	public static final String ANNO_PROPERTIES = "anno:properties"; //$NON-NLS-1$
	public static final String ANNO_HEAD = "anno:head"; //$NON-NLS-1$
	public static final String ANNO_SPEAKER = "anno:speaker"; //$NON-NLS-1$
}
