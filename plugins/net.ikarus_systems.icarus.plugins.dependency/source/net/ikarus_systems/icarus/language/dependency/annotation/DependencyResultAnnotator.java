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

import net.ikarus_systems.icarus.language.dependency.DependencyData;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.result.Hit;
import net.ikarus_systems.icarus.search_tools.result.ResultEntry;
import net.ikarus_systems.icarus.search_tools.result.SearchResult;
import net.ikarus_systems.icarus.search_tools.tree.AbstractTreeResultAnnotator;
import net.ikarus_systems.icarus.search_tools.tree.Matcher;
import net.ikarus_systems.icarus.util.annotation.AbstractAnnotation;
import net.ikarus_systems.icarus.util.annotation.AnnotatedData;
import net.ikarus_systems.icarus.util.annotation.Annotation;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyResultAnnotator extends AbstractTreeResultAnnotator {

	public DependencyResultAnnotator(Matcher rootMatcher) {
		super(rootMatcher);
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.annotation.ResultAnnotator#annotate(net.ikarus_systems.icarus.search_tools.result.SearchResult, java.lang.Object, net.ikarus_systems.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	public AnnotatedData annotate(SearchResult searchResult, Object data,
			ResultEntry entry) {
		if(data instanceof DependencyData) {
			Annotation annotation = new DependencyAnnotation(entry);
			return new AnnotatedDependencyData(
					(DependencyData) data, annotation);
		} else
			throw new IllegalArgumentException("Unable to annotate unsupported data: "+data.getClass()); //$NON-NLS-1$
	}
	
	protected long[] createHighlights(Hit hit) {
		long[] highlights = new long[hit.getIndexCount()];
		for(int i=0; i<hit.getIndexCount(); i++) {
			highlights[i] = createHighlight(matchers[i].getConstraints());
		}
		return highlights;
	}
	
	protected long createHighlight(SearchConstraint[] constraints) {
		// TODO
	}

	/**
	 * Special annotation implementation for dependency data
	 * using lazy creation of the actual annotation data. 
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected class DependencyAnnotation extends AbstractAnnotation {
		
		protected final ResultEntry entry;
		
		protected long[][] highlights;
		
		public DependencyAnnotation(ResultEntry entry) {
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
		
		protected long[] getHighlightSet() {
			if(isBeforeFirst() || isAfterLast())
				throw new IllegalStateException();
			
			if(highlights==null) {
				highlights = new long[getAnnotationCount()][];
			}
			
			int index = getIndex();
			
			if(highlights[index]==null) {
				highlights[index] = createHighlights(entry.getHit(index));
			}
			
			return highlights[index];
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

		public long getHighlight(int index) {
			Hit hit = entry.getHit(getIndex());
			for(int i=0; i<hit.getIndexCount(); i++) {
				if(hit.getIndex(i)==index) {
					// Only generate the highlight info for this hit if
					// the index in question actually needs highlighting
					return getHighlightSet()[i];
				}
			}
			return 0;
		}
	}
}
