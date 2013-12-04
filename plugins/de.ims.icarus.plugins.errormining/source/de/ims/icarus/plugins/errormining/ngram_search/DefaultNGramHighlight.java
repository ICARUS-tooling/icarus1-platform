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
package de.ims.icarus.plugins.errormining.ngram_search;

import java.util.BitSet;

import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator.Highlight;
import de.ims.icarus.util.collections.CollectionUtils;

public class DefaultNGramHighlight implements Highlight {
	
	protected BitSet highlightedIndices;
	protected int[] indexMap;
	protected final long ngramHighlight;
	protected final long ngramHeadHighlight = BitmaskHighlighting.NODE_HIGHLIGHT;
	protected boolean highlightEdge;
	
	protected int[] dependencyInfo;
	
	public DefaultNGramHighlight(int[] indexMap, boolean highlightEdge) {
		
		int size = CollectionUtils.max(indexMap);
		highlightedIndices = new BitSet(size);
		
		this.indexMap = indexMap;
		this.highlightEdge = highlightEdge;
		
		if(highlightEdge){
			ngramHighlight = BitmaskHighlighting.NODE_HIGHLIGHT | BitmaskHighlighting.EDGE_HIGHLIGHT;
		} else {
			ngramHighlight = BitmaskHighlighting.NODE_HIGHLIGHT;
		}
		
		for(int index : indexMap) {
			if(index!=-1) {
				highlightedIndices.set(index);
			}
		}		
	}
	
	

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator.Highlight#getHighlight(int)
	 */
	@Override
	public long getHighlight(int index) {
		if(highlightedIndices.get(index)) {
			for(int i=0; i<indexMap.length; i++) {
				if(indexMap[i]==index) {
					//dependency highlight always dependend, head node)
					if(highlightEdge && (i % 2 != 0)){
						return ngramHeadHighlight;
					}else {
						return ngramHighlight;
					}
				}
			}
		}			
		return 0L;
	}
	
}