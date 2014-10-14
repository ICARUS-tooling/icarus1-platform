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
package de.ims.icarus.search_tools.annotation;

import java.util.BitSet;

import de.ims.icarus.search_tools.result.Hit;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.util.annotation.AbstractAnnotation;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLazyResultAnnotator implements ResultAnnotator {

	protected long[] baseHighlights;

	protected final BitmaskHighlighting highlighting;

	protected AbstractLazyResultAnnotator(BitmaskHighlighting highlighting) {
		if(highlighting==null)
			throw new NullPointerException("Invalid highlighting"); //$NON-NLS-1$

		this.highlighting = highlighting;
	}

	public BitmaskHighlighting getHighlighting() {
		return highlighting;
	}

	public abstract int getHighlightCount();

	protected abstract long createBaseHighlight(int index);

	public long[] getBaseHighlights() {
		if(baseHighlights==null) {
			baseHighlights = new long[getHighlightCount()];
			for(int i=0; i<baseHighlights.length; i++) {
				baseHighlights[i] = createBaseHighlight(i);
			}
		}
		return baseHighlights;
	}

	protected abstract boolean supports(Object data);

	@Override
	public AnnotatedData annotate(SearchResult searchResult, Object data,
			ResultEntry entry) {

		if(supports(data)) {
			return createAnnotatedData(data, entry);
		} else if(data!=null)
			throw new IllegalArgumentException("Unable to annotate unsupported data: "+data.getClass()); //$NON-NLS-1$

		return null;
	}

	protected Annotation createAnnotation(Object data, ResultEntry entry) {
		return new LazyAnnotation(data, entry);
	}

	protected abstract AnnotatedData createAnnotatedData(Object data, ResultEntry entry);


	protected abstract Highlight createHighlight(Object data, Hit hit);

	public interface Highlight {

		public long getHighlight(int index);

		public boolean isHighlighted(int index);

		public int getMatcherId(int index);
	}

	public static class DefaultHighlight implements Highlight {
		protected BitSet highlightedIndices;
		protected int[] indexMap;
		protected long[] highlights;

		public DefaultHighlight(int[] indexMap, long[] highlights) {
			int size = CollectionUtils.max(indexMap)+1;
			highlightedIndices = new BitSet(size);

			this.indexMap = indexMap;
			this.highlights = highlights;

			for(int index : indexMap) {
				if(index!=-1) {
					highlightedIndices.set(index);
				}
			}
		}

		@Override
		public long getHighlight(int index) {
			if(highlightedIndices.get(index)) {
				for(int i=0; i<indexMap.length; i++) {
					if(indexMap[i]==index) {
						return highlights[i];
					}
				}
			}

			return 0L;
		}

		/**
		 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator.Highlight#isHighlighted(int)
		 */
		@Override
		public boolean isHighlighted(int index) {
			return highlightedIndices.get(index);
		}

		/**
		 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator.Highlight#getMatcherId(int)
		 */
		@Override
		public int getMatcherId(int index) {
			if(highlightedIndices.get(index)) {
				for(int i=0; i<indexMap.length; i++) {
					if(indexMap[i]==index) {
						return i;
					}
				}
			}
			return -1;
		}
	}

	public class LazyAnnotation extends AbstractAnnotation implements ResultAnnotation, SearchAnnotation {

		protected final ResultEntry entry;
		protected final Object data;

		protected Highlight[] highlights;

		public LazyAnnotation(Object data, ResultEntry entry) {
			this.data = data;
			this.entry = entry;
		}

		public ResultEntry getEntry() {
			return entry;
		}

		public Hit getHit() {
			if(isBeforeFirst() || isAfterLast())
				throw new IllegalStateException();

			return entry.getHit(getIndex());
		}

		/**
		 * @see de.ims.icarus.util.annotation.Annotation#getAnnotationCount()
		 */
		@Override
		public int getAnnotationCount() {
			return entry.getHitCount();
		}

		public Highlight getHighlight() {
			if(isBeforeFirst() || isAfterLast())
				throw new IllegalStateException();

			if(highlights==null) {
				highlights = new Highlight[getAnnotationCount()];
			}

			if(highlights[getIndex()]==null) {
				highlights[getIndex()] = getAnnotator().createHighlight(data, getHit());
			}

			return highlights[getIndex()];
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#getGroupId(int)
		 */
		@Override
		public int getGroupId(int index) {
			return getHighlighting().getGroupId(getHighlight(index));
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#getGroupId(int)
		 */
		@Override
		public int getGroupId(int index, String token) {
			return getHighlighting().getGroupId(getHighlight(index), token);
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#isHighlighted(int)
		 */
		@Override
		public boolean isHighlighted(int index) {
			return getHighlight(index)!=0L;
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#isNodeHighlighted(int)
		 */
		@Override
		public boolean isNodeHighlighted(int index) {
			return getHighlighting().isNodeHighlighted(getHighlight(index));
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#isEdgeHighlighted(int)
		 */
		@Override
		public boolean isEdgeHighlighted(int index) {
			return getHighlighting().isEdgeHighlighted(getHighlight(index));
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#isTransitiveHighlighted(int)
		 */
		@Override
		public boolean isTransitiveHighlighted(int index) {
			return getHighlighting().isTransitiveHighlighted(getHighlight(index));
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#getHighlight(int)
		 */
		@Override
		public long getHighlight(int index) {
			return getHighlight().getHighlight(index);
		}

		/**
		 * @see de.ims.icarus.language.dependency.annotation.DependencyAnnotation#isTokenHighlighted(int, java.lang.String)
		 */
		@Override
		public boolean isTokenHighlighted(int index, String token) {
			return getHighlighting().isTokenHighlighted(getHighlight(index), token);
		}

		/**
		 * @see de.ims.icarus.search_tools.annotation.ResultAnnotation#getResultEntry()
		 */
		@Override
		public ResultEntry getResultEntry() {
			return entry;
		}

		/**
		 * @see de.ims.icarus.search_tools.annotation.SearchAnnotation#getAnnotator()
		 */
		@Override
		public AbstractLazyResultAnnotator getAnnotator() {
			return AbstractLazyResultAnnotator.this;
		}
	}
}
