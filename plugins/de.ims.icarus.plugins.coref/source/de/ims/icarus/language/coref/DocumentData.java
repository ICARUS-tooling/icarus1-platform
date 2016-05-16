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
package de.ims.icarus.language.coref;

import de.ims.icarus.language.SentenceDataList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface DocumentData extends SentenceDataList {

	public static final String DOCUMENT_ID_PROPERTY = "documentId"; //$NON-NLS-1$
	public static final String DOCUMENT_HEADER_PROPERTY = "documentHeader"; //$NON-NLS-1$
	
	@Override
	public CoreferenceData get(int index);
	
	public void add(CoreferenceData data);

	public DocumentSet getDocumentSet();
	
	public SpanSet getSpanSet();
	
	public EdgeSet getEdgeSet();
	
	public SpanSet getDefaultSpanSet();
	
	public EdgeSet getDefaultEdgeSet();

	public int getDocumentIndex();

	public String getId();
	
	public Object getProperty(String key);
}
