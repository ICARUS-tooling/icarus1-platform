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
package de.ims.icarus.plugins.prosody.annotation;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.prosody.ProsodicSentenceData;
import de.ims.icarus.plugins.prosody.ProsodyUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.plugins.prosody.search.constraints.SyllableConstraint;
import de.ims.icarus.plugins.prosody.search.constraints.painte.PaIntEConstraint;
import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchEdge;
import de.ims.icarus.search_tools.annotation.BitmaskHighlighting;
import de.ims.icarus.search_tools.result.Hit;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.tree.AbstractTreeResultAnnotator;
import de.ims.icarus.search_tools.tree.DummyGroupConstraint;
import de.ims.icarus.search_tools.tree.Matcher;
import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.data.ContentType;
import gnu.trove.list.TIntList;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyResultAnnotator extends AbstractTreeResultAnnotator {

	protected final boolean hasSyllableConstraints;
	protected final boolean hasPaIntEConstraints;

	public ProsodyResultAnnotator(Matcher rootMatcher) {
		this(ProsodyHighlighting.getInstance(), rootMatcher);
	}

	public ProsodyResultAnnotator(BitmaskHighlighting highlighting, Matcher rootMatcher) {
		super(highlighting, rootMatcher);

		boolean hasSyllableConstraints = false;
		boolean hasPaIntEConstraints = false;

		for(Matcher matcher : getMatchers()) {
			SearchConstraint[] constraints = matcher.getConstraints();
			if(constraints==null || constraints.length==0) {
				continue;
			}
			for(SearchConstraint constraint : constraints) {
				if(constraint instanceof DummyGroupConstraint) {
					constraint = ((DummyGroupConstraint)constraint).getSource();
				}

				if(constraint instanceof SyllableConstraint) {
					hasSyllableConstraints = true;
				}
				if(constraint instanceof PaIntEConstraint) {
					hasPaIntEConstraints = true;
				}
			}

			if(hasSyllableConstraints && hasPaIntEConstraints) {
				break;
			}
		}

		this.hasSyllableConstraints = hasSyllableConstraints;
		this.hasPaIntEConstraints = hasPaIntEConstraints;
	}

	public boolean hasSyllableConstraints() {
		return hasSyllableConstraints;
	}

	public boolean hasPaIntEConstraints() {
		return hasPaIntEConstraints;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.ResultAnnotator#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return ProsodyUtils.getProsodyAnnotationType();
	}

	/**
	 * @see de.ims.icarus.search_tools.tree.AbstractTreeResultAnnotator#getHead(java.lang.Object, int)
	 */
	@Override
	protected int getHead(Object data, int index) {
		return ((ProsodicSentenceData)data).getHead(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#supports(java.lang.Object)
	 */
	@Override
	protected boolean supports(Object data) {
		return data instanceof ProsodicSentenceData;
	}

	/**
	 * @see de.ims.icarus.search_tools.annotation.AbstractLazyResultAnnotator#createAnnotatedData(java.lang.Object, de.ims.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	protected AnnotatedProsodicSentenceData createAnnotatedData(Object data, ResultEntry entry) {
		return new LazyAnnotatedProsodicData((ProsodicSentenceData) data, entry);
	}

	@Override
	protected ProsodicAnnotation createAnnotation(Object data, ResultEntry entry) {
		return new LazyProsodicAnnotation(data, entry);
	}

	@Override
	public ProsodyHighlighting getHighlighting() {
		return (ProsodyHighlighting) super.getHighlighting();
	}

	private static transient List<SearchConstraint> constraintBuffer = new ArrayList<>();

	protected SearchConstraint[] getConstraints(int matcherId, Class<?> constraintClass) {
		if(matcherId==-1) {
			return null;
		}

		Matcher matcher = getMatcher(matcherId);
		SearchConstraint[] constraints = matcher.getConstraints();

		if(constraints==null || constraints.length==0) {
			return null;
		}

		for(SearchConstraint constraint : constraints) {
			if(constraint instanceof DummyGroupConstraint) {
				constraint = ((DummyGroupConstraint)constraint).getSource();
			}
			if(constraintClass.isInstance(constraint)) {
				constraintBuffer.add(constraint);
			}
		}

		if(constraintBuffer.isEmpty()) {
			return null;
		}

		SearchConstraint[] result = constraintBuffer.toArray(new SearchConstraint[constraintBuffer.size()]);

		constraintBuffer.clear();

		return result;
	}

	private final transient ProsodyTargetTree targetTree = new ProsodyTargetTree();

	@Override
	protected synchronized Highlight createHighlight(Object data, Hit hit) {
		// Flexible buffer structures to allow for addition of
		// needed highlight data during construction process
		TIntList indexMap = new TIntArrayList(hit.getIndexCount());
		TLongList highlights = new TLongArrayList(hit.getIndexCount());

		ProsodicSentenceData sentence = (ProsodicSentenceData) data;

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

		if(!hasSyllableConstraints) {
			return new DefaultHighlight(_indexMap, _highlights);
		}

		// Run micro search to get highlight info on syllable level
		targetTree.reload(sentence, null);

		long[][] _sylHighlights = new long[_highlights.length][];

		for(int matcherId=0; matcherId<_indexMap.length; matcherId++) {
			int wordIndex = _indexMap[matcherId];
			if(wordIndex==-1) {
				continue;
			}

			int sylCount = sentence.getSyllableCount(wordIndex);

			if(sylCount==0) {
				continue;
			}

			Matcher matcher = matchers[matcherId];

			SearchConstraint[] constraints = matcher.getConstraints();
			if(constraints==null || constraints.length==0) {
				continue;
			}

			targetTree.viewNode(wordIndex);

			long[] sylHl = new long[sylCount];

			for(int i=0; i<sylCount; i++) {
				sylHl[i] = getHighlighting().getHighlight(constraints, targetTree, i);
			}

			_sylHighlights[matcherId] = sylHl;
		}

		return new DefaultSyllableHighlight(_indexMap, _highlights, _sylHighlights);
	}

	protected class LazyAnnotatedProsodicData extends AnnotatedProsodicSentenceData {

		private static final long serialVersionUID = 5071999017012952840L;

		private final ResultEntry entry;

		public LazyAnnotatedProsodicData(ProsodicSentenceData source, ResultEntry entry) {
			super(source);

			this.entry = entry;
		}

		@Override
		public ProsodicAnnotation getAnnotation() {
			Annotation annotation = super.getAnnotation();

			if(annotation==null) {
				annotation = createAnnotation(this, entry);
				setAnnotation(annotation);
			}

			return (ProsodicAnnotation) annotation;
		}
	}

	public interface SyllableHighlight extends Highlight {

		public long getHighlight(int index, int sylIndex);
	}

	public static class DefaultSyllableHighlight extends DefaultHighlight implements SyllableHighlight {
		protected long[][] sylHighlights;

		public DefaultSyllableHighlight(int[] indexMap, long[] highlights, long[][] sylHighlights) {
			super(indexMap, highlights);

			this.sylHighlights = sylHighlights;
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
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodyResultAnnotator.SyllableHighlight#getHighlight(int, int)
		 */
		@Override
		public long getHighlight(int index, int sylIndex) {
			if(highlightedIndices.get(index)) {
				for(int i=0; i<indexMap.length; i++) {
					if(indexMap[i]==index) {
						long[] hl = sylHighlights[i];
						return (hl==null || hl.length<sylIndex) ? 0L : hl[sylIndex];
					}
				}
			}

			return 0L;
		}
	}

	protected class LazyProsodicAnnotation extends LazyAnnotation implements ProsodicAnnotation {

		public LazyProsodicAnnotation(Object data, ResultEntry entry) {
			super(data, entry);
		}

		@Override
		public ProsodyResultAnnotator getAnnotator() {
			return ProsodyResultAnnotator.this;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#isHighlighted(int, int)
		 */
		@Override
		public boolean isHighlighted(int index, int sylIndex) {
			return getHighlight(index, sylIndex)!=0L;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#getGroupId(int, int)
		 */
		@Override
		public int getGroupId(int index, int sylIndex) {
			return getHighlighting().getGroupId(getHighlight(index, sylIndex));
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#getGroupId(int, int, java.lang.String)
		 */
		@Override
		public int getGroupId(int index, int sylIndex, String token) {
			return getHighlighting().getGroupId(getHighlight(index, sylIndex), token);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#isNodeHighlighted(int, int)
		 */
		@Override
		public boolean isNodeHighlighted(int index, int sylIndex) {
			return getHighlighting().isNodeHighlighted(getHighlight(index, sylIndex));
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#isEdgeHighlighted(int, int)
		 */
		@Override
		public boolean isEdgeHighlighted(int index, int sylIndex) {
			return getHighlighting().isEdgeHighlighted(getHighlight(index, sylIndex));
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#isTransitiveHighlighted(int, int)
		 */
		@Override
		public boolean isTransitiveHighlighted(int index, int sylIndex) {
			return getHighlighting().isTransitiveHighlighted(getHighlight(index, sylIndex));
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#getHighlight(int, int)
		 */
		@Override
		public long getHighlight(int index, int sylIndex) {
			Highlight highlight = getHighlight();
			return highlight instanceof SyllableHighlight ? ((SyllableHighlight)highlight).getHighlight(index, sylIndex) : 0L;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.annotation.ProsodicAnnotation#isTokenHighlighted(int, int, java.lang.String)
		 */
		@Override
		public boolean isTokenHighlighted(int index, int sylIndex, String token) {
			return getHighlighting().isTokenHighlighted(getHighlight(index, sylIndex), token);
		}

		@Override
		public <S> SearchConstraint[] getConstraints(int index,
				Class<S> constraintClass) {
			Highlight highlight = getHighlight();
			if(highlight.getHighlight(index)==0L) {
				return null;
			}

			return getAnnotator().getConstraints(highlight.getMatcherId(index), constraintClass);
		}

	}
}
