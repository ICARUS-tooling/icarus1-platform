/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.annotation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.language.dependency.DependencyUtils;
import net.ikarus_systems.icarus.search_tools.EdgeType;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchEdge;
import net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotation;
import net.ikarus_systems.icarus.search_tools.result.Hit;
import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.tree.AbstractTreeResultAnnotator;
import net.ikarus_systems.icarus.search_tools.tree.Matcher;
import net.ikarus_systems.icarus.search_tools.util.SearchUtils;
import net.ikarus_systems.icarus.util.CollectionUtils;
import net.ikarus_systems.icarus.util.annotation.AbstractAnnotation;
import net.ikarus_systems.icarus.util.annotation.AnnotatedData;
import net.ikarus_systems.icarus.util.annotation.Annotation;
import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyResultAnnotator extends AbstractTreeResultAnnotator {
	
	protected long[] baseHighlights;

	public DependencyResultAnnotator(Matcher rootMatcher) {
		super(rootMatcher);
		
		baseHighlights = new long[matchers.length];
		for(int i=0; i<matchers.length; i++) {
			baseHighlights[i] = createBaseHighlight(matchers[i]);
		}
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotator#annotate(net.ikarus_systems.icarus.search_tools.result.SearchResult, java.lang.Object, net.ikarus_systems.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	public AnnotatedData annotate(SearchResult searchResult, Object data,
			ResultEntry entry) {
		if(data instanceof DependencyData) {
			return new LazyAnnotatedDependencyData((DependencyData) data, entry);
		} else if(data!=null)
			throw new IllegalArgumentException("Unable to annotate unsupported data: "+data.getClass()); //$NON-NLS-1$
		
		return null;
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotator#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return DependencyUtils.getDependencyAnnotationType();
	}
	
	protected Annotation createAnnotation(DependencyData data, ResultEntry entry) {
		return new LazyDependencyAnnotation(data, entry);
	}
	
	protected long createBaseHighlight(Matcher matcher) {
		boolean highlightEdge = !SearchUtils.isUndefined(matcher.getEdge());
		return DependencyHighlighting.getHighlight(
				matcher.getConstraints(), true, highlightEdge);
	}
	
	protected Highlight createHighlight(DependencyData data, Hit hit) {
		// Flexible buffer structures to allow for addition of
		// needed highlight data during construction process
		List<Integer> indexMap = new ArrayList<>(hit.getIndexCount());
		List<Long> highlights = new ArrayList<>(hit.getIndexCount());
		
		boolean trans = false;
		
		// First pass -> plain copying of highlight info
		for(int i=0; i<hit.getIndexCount(); i++) {
			indexMap.add(hit.getIndex(i));
			highlights.add(baseHighlights[i]);
			
			SearchEdge edge = matchers[i].getEdge();
			if(edge!=null && edge.getEdgeType()==EdgeType.TRANSITIVE) {
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
				highlight |= DependencyHighlighting.TRANSITIVE_HIGHLIGHT;
				highlights.set(i, highlight);
				
				int parentIndex = hit.getIndex(matcher.getParent().getId());
				
				int index = hit.getIndex(i);
				// Traverse up all the way to the parent index
				while(index!=parentIndex) {
					int head = data.getHead(index);
					if(LanguageUtils.isRoot(head) || LanguageUtils.isUndefined(head)) {
						break;
					}
					
					// Mark intermediate edge as transitive
					highlight = DependencyHighlighting.GENERAL_HIGHLIGHT;
					highlight |= DependencyHighlighting.TRANSITIVE_HIGHLIGHT;
					
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
		
		return new Highlight(_indexMap, _highlights);
	}
	
	protected class LazyAnnotatedDependencyData extends AnnotatedDependencyData {

		private static final long serialVersionUID = -1463625267475398824L;
		private final ResultEntry entry;

		public LazyAnnotatedDependencyData(DependencyData source, ResultEntry entry) {
			super(source);
			
			this.entry = entry;
		}

		@Override
		public Annotation getAnnotation() {
			Annotation annotation = super.getAnnotation();
			
			if(annotation==null) {
				annotation = createAnnotation(this, entry);
			}
			
			return annotation;
		}
		
	}
	
	public static class Highlight {
		protected BitSet highlightedIndices;
		protected int[] indexMap;
		protected long[] highlights;
		
		public Highlight(int[] indexMap, long[] highlights) {
			int size = CollectionUtils.max(indexMap);
			highlightedIndices = new BitSet(size);
			
			this.indexMap = indexMap;
			this.highlights = highlights;
			
			for(int index : indexMap) {
				if(index!=-1) {
					highlightedIndices.set(index);
				}
			}
		}
		
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
	}

	public class LazyDependencyAnnotation extends AbstractAnnotation implements DependencyAnnotation, ResultAnnotation {
		
		protected final ResultEntry entry;
		protected final DependencyData data;
		
		protected Highlight[] highlights;
		
		public LazyDependencyAnnotation(DependencyData data, ResultEntry entry) {
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
		 * @see net.ikarus_systems.icarus.util.annotation.Annotation#getAnnotationCount()
		 */
		@Override
		public int getAnnotationCount() {
			return entry.getHitCount();
		}
		
		public Matcher getMatcher(int index) {
			Hit hit = entry.getHit(getIndex());
			for(int i=0; i<hit.getIndexCount(); i++) {
				if(hit.getIndex(i)==index) {
					return matchers[i];
				}
			}
			return null;
		}
		
		public SearchConstraint[] getConstraints(int index) {
			Hit hit = entry.getHit(getIndex());
			for(int i=0; i<hit.getIndexCount(); i++) {
				if(hit.getIndex(i)==index) {
					return matchers[i].getConstraints();
				}
			}
			return null;
		}

		public Highlight getHighlight() {
			if(isBeforeFirst() || isAfterLast())
				throw new IllegalStateException();
			
			if(highlights==null) {
				highlights = new Highlight[getAnnotationCount()];
			}
			
			if(highlights[getIndex()]==null) {
				highlights[getIndex()] = createHighlight(data, getHit());
			}
			
			return highlights[getIndex()];
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#getGroupId(int)
		 */
		@Override
		public int getGroupId(int index) {
			return DependencyHighlighting.getGroupId(getHighlight(index));
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#getGroupId(int)
		 */
		@Override
		public int getGroupId(int index, String token) {
			return DependencyHighlighting.getGroupId(getHighlight(index), token);
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#isHighlighted(int)
		 */
		@Override
		public boolean isHighlighted(int index) {
			return getHighlight(index)!=0L;
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#isNodeHighlighted(int)
		 */
		@Override
		public boolean isNodeHighlighted(int index) {
			return DependencyHighlighting.isNodeHighlighted(getHighlight(index));
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#isEdgeHighlighted(int)
		 */
		@Override
		public boolean isEdgeHighlighted(int index) {
			return DependencyHighlighting.isEdgeHighlighted(getHighlight(index));
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#isTransitiveHighlighted(int)
		 */
		@Override
		public boolean isTransitiveHighlighted(int index) {
			return DependencyHighlighting.isTransitiveHighlighted(getHighlight(index));
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#getHighlight(int)
		 */
		@Override
		public long getHighlight(int index) {
			return getHighlight().getHighlight(index);
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#isTokenHighlighted(int, java.lang.String)
		 */
		@Override
		public boolean isTokenHighlighted(int index, String token) {
			return DependencyHighlighting.isTokenHighlighted(getHighlight(index), token);
		}

		/**
		 * @see net.ikarus_systems.icarus.language.dependency.annotation.DependencyAnnotation#getCorpusIndex()
		 */
		@Override
		public int getCorpusIndex() {
			return entry.getIndex();
		}

		/**
		 * @see net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotation#getResultEntry()
		 */
		@Override
		public ResultEntry getResultEntry() {
			return entry;
		}
	}
}
