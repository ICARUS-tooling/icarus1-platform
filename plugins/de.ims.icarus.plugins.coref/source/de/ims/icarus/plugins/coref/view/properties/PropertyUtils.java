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
package de.ims.icarus.plugins.coref.view.properties;

import de.ims.icarus.language.coref.CorefProperties;
import de.ims.icarus.language.coref.CoreferenceAllocation;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.CoreferenceDocumentData;
import de.ims.icarus.language.coref.CoreferenceDocumentSet;
import de.ims.icarus.language.coref.Edge;
import de.ims.icarus.language.coref.EdgeSet;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.language.coref.SpanSet;
import de.ims.icarus.util.Counter;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class PropertyUtils {

	private PropertyUtils() {
		// no-op
	}

	public static void countProperties(Counter counter, CoreferenceData data) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(data==null) {
			return;
		}
		
		CorefProperties.countKeys(data.getProperties(), counter);
	}

	public static void countProperties(Counter counter, CoreferenceDocumentData document) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(document==null || document.size()==0) {
			return;
		}
		
		for(int i=0; i<document.size(); i++) {
			CoreferenceData sentence = document.get(i);
			CorefProperties.countKeys(sentence.getProperties(), counter);
		}
	}

	public static void countProperties(Counter counter, CoreferenceDocumentSet documentSet) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(documentSet==null || documentSet.size()==0) {
			return;
		}
		
		for(int i=0; i<documentSet.size(); i++) {
			countProperties(counter, documentSet.get(i));
		}
	}

	public static void countProperties(Counter counter, SpanSet spanSet) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(spanSet==null || spanSet.size()==0) {
			return;
		}
		
		for(int i=0; i<spanSet.size(); i++) {
			Span span = spanSet.get(i);
			CorefProperties.countKeys(span.getProperties(), counter);
		}
	}

	public static void countProperties(Counter counter, EdgeSet edgeSet) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(edgeSet==null || edgeSet.size()==0) {
			return;
		}
		
		for(int i=0; i<edgeSet.size(); i++) {
			Edge edge = edgeSet.get(i);
			CorefProperties.countKeys(edge.getProperties(), counter);
		}
	}

	public static void countProperties(Counter counter, CoreferenceAllocation allocation) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(allocation==null || allocation.size()==0) {
			return;
		}
		
		String[] documentIds = allocation.getDocumentIds();
		for(String documentId : documentIds) {
			countProperties(counter, allocation.getSpanSet(documentId));
			countProperties(counter, allocation.getEdgeSet(documentId));
		}
	}

	public static void countSpanProperties(Counter counter, CoreferenceAllocation allocation) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(allocation==null || allocation.size()==0) {
			return;
		}
		
		String[] documentIds = allocation.getDocumentIds();
		for(String documentId : documentIds) {
			countProperties(counter, allocation.getEdgeSet(documentId));
		}
	}

	public static void countEdgeProperties(Counter counter, CoreferenceAllocation allocation) {
		if(counter==null)
			throw new NullPointerException("Invalid counter"); //$NON-NLS-1$
		if(allocation==null || allocation.size()==0) {
			return;
		}
		
		String[] documentIds = allocation.getDocumentIds();
		for(String documentId : documentIds) {
			countProperties(counter, allocation.getEdgeSet(documentId));
		}
	}
}
