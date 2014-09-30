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
package de.ims.icarus.search_tools.tree;

import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;

import java.util.List;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.search_tools.result.Hit;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.CorruptedStateException;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractTreeResultAnnotator extends AbstractLazyResultAnnotator {

	protected Matcher[] matchers;

	protected AbstractTreeResultAnnotator(BitmaskHighlighting highlighting, Matcher rootMatcher) {
		super(highlighting);

		List<Matcher> buffer = TreeUtils.collectMatchers(rootMatcher);
		TreeUtils.clearDuplicates(buffer);

		matchers = buffer.toArray(new Matcher[0]);

		for(int i=0; i<matchers.length; i++) {
			if(matchers[i].getId()!=i)
				throw new CorruptedStateException();
		}
	}

	public Matcher[] getMatchers() {
		return matchers;
	}

	@Override
	public int getHighlightCount() {
		return matchers.length;
	}

	@Override
	protected long createBaseHighlight(int index) {
		Matcher matcher = matchers[index];
		boolean highlightEdge = !SearchUtils.isUndefined(matcher.getEdge());
		return getHighlighting().getHighlight(matcher.getConstraints(), true, highlightEdge);
	}

	protected abstract int getHead(Object data, int index);

	@Override
	protected Highlight createHighlight(Object data, Hit hit) {
		// Flexible buffer structures to allow for addition of
		// needed highlight data during construction process
		TIntList indexMap = new TIntArrayList(hit.getIndexCount());
		TLongList highlights = new TLongArrayList(hit.getIndexCount());

		boolean trans = false;
		long[] baseHighlights = getBaseHighlights();

		// First pass -> plain copying of highlight info
		for(int i=0; i<hit.getIndexCount(); i++) {
			indexMap.add(hit.getIndex(i));
			highlights.add(baseHighlights[i]);

			SearchEdge edge = matchers[i].getEdge();
			if(!trans && edge!=null && edge.getEdgeType()==EdgeType.TRANSITIVE) {
				trans = true;
			}
		}

		// Second pass if required
		if(trans) {
			for(int i=0; i<hit.getIndexCount(); i++) {
				Matcher matcher = matchers[i];
				if(matcher.getEdge()==null
						|| matcher.getEdge().getEdgeType()!=EdgeType.TRANSITIVE) {
					continue;
				}

				// Add transitive flag to existing highlight
				long highlight = highlights.get(i);
				highlight |= BitmaskHighlighting.TRANSITIVE_HIGHLIGHT;
				highlights.set(i, highlight);

				int parentIndex = hit.getIndex(matcher.getParent().getId());

				int index = hit.getIndex(i);
				// Traverse up all the way to the parent index
				while(index!=parentIndex) {
					int head = getHead(data, index);
					if(LanguageUtils.isRoot(head) || LanguageUtils.isUndefined(head)) {
						break;
					}

					// Mark intermediate edge as transitive
					highlight = BitmaskHighlighting.GENERAL_HIGHLIGHT;
					highlight |= BitmaskHighlighting.TRANSITIVE_HIGHLIGHT;

					// Add 'new' highlight entry
					highlights.add(highlight);
					indexMap.add(index);

					index = head;
				}
			}
		}

		// Create final buffer structures
		int size = indexMap.size();
		int[]_indexMap = new int[size];
		long[] _highlights = new long[size];
		for(int i=0; i<size; i++) {
			_indexMap[i] = indexMap.get(i);
			_highlights[i] = highlights.get(i);
		}

		return new DefaultHighlight(_indexMap, _highlights);
	}
}
