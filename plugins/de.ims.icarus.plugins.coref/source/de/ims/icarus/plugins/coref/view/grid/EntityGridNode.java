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
package de.ims.icarus.plugins.coref.view.grid;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import de.ims.icarus.language.coref.CorefErrorType;
import de.ims.icarus.language.coref.CoreferenceData;
import de.ims.icarus.language.coref.Span;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class EntityGridNode {
	
	private final CoreferenceData sentence;
	private final Span[] spans;
	private final CorefErrorType[] types;
	private final Color[] highlightColors;

	public EntityGridNode(CoreferenceData sentence, Span[] spans, CorefErrorType[] types, Color[] highlightColors) {
		if(sentence==null)
			throw new NullPointerException("Invalid sentence"); //$NON-NLS-1$
		if(spans==null)
			throw new NullPointerException("Invalid spans array"); //$NON-NLS-1$
		if(types==null)
			throw new NullPointerException("Invalid types array"); //$NON-NLS-1$
		if(spans.length!=types.length)
			throw new IllegalArgumentException("Size mismatch between spans and types"); //$NON-NLS-1$
		
		this.sentence = sentence;
		this.spans = spans;
		this.types = types;
		this.highlightColors = highlightColors;
	}
	
	public CoreferenceData getSentence() {
		return sentence;
	}

	public int getSpanCount() {
		return spans.length;
	}
	
	public Span getSpan(int index) {
		return spans[index];
	}
	
	public CorefErrorType getErrorType(int index) {
		return types[index];
	}
	
	public List<Span> getSpans() {
		return CollectionUtils.asList(spans);
	}
	
	public List<CorefErrorType> getTypes() {
		return CollectionUtils.asList(types);
	}
		
	
//	public boolean isFalsePositive(int index) {
//		return types[index]==ErrorType.FALSE_POSITIVE;
//	}
//	
//	public boolean isFalseNegative(int index) {
//		return types[index]==ErrorType.FALSE_NEGATIVE;
//	}
//	
//	public boolean hasFalsePositive() {
//		for(ErrorType type : types) {
//			if(type==ErrorType.FALSE_POSITIVE) {
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	public boolean hasFalseNegative() {
//		for(ErrorType type : types) {
//			if(type==ErrorType.FALSE_NEGATIVE) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	public Color getHighlightColor(int index) {
		return highlightColors==null ? null : highlightColors[index];
	}
	
	public boolean isHighlighted(int index) {
		return highlightColors!=null && highlightColors[index]!=null;
	}
	
	public boolean hasHighlightedSpan() {
		if(highlightColors==null) {
			return false;
		}
		for(Color col : highlightColors) {
			if(col!=null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof EntityGridNode) {
			EntityGridNode other = (EntityGridNode)obj;
			return Arrays.equals(spans, other.spans);
		}
		return false;
	}

	@Override
	public String toString() {
		return Arrays.toString(spans);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Arrays.hashCode(spans);
	}
}
