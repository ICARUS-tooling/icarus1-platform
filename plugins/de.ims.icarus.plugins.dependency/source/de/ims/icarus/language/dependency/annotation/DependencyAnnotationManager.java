/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency.annotation;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.dependency.DependencyUtils;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;
import de.ims.icarus.util.annotation.AnnotationManager;
import de.ims.icarus.util.data.ContentType;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyAnnotationManager extends AnnotationManager {

	public DependencyAnnotationManager() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.util.annotation.AnnotationManager#getAnnotationType()
	 */
	@Override
	public ContentType getAnnotationType() {
		return DependencyUtils.getDependencyAnnotationType();
	}

	@Override
	public DependencyAnnotation getAnnotation() {
		return (DependencyAnnotation) super.getAnnotation();
	}
	
	protected AnnotationDelegate getDelegate() {
		return (AnnotationDelegate) delegate;
	}

	public int getGroupId(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? -1 : delegate.getGroupId(
				getAnnotation(), index, getPosition());
	}
	
	public int getGroupId(int index, String token) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? -1 : delegate.getGroupId(
				getAnnotation(), index, token, getPosition());
	}
	
	public boolean isHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isHighlighted(
				getAnnotation(), index, getPosition());
	}
	
	public boolean isNodeHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isNodeHighlighted(
				getAnnotation(), index, getPosition());
	}
	
	public boolean isEdgeHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isEdgeHighlighted(
				getAnnotation(), index, getPosition());
	}
	
	public boolean isTransitiveHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isTransitiveHighlighted(
				getAnnotation(), index, getPosition());
	}
	
	public long getHighlight(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? 0L : delegate.getHighlight(
				getAnnotation(), index, getPosition());
	}
	
	public boolean isTokenHighlighted(int index, String token) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isTokenHighlighted(
				getAnnotation(), index, token, getPosition());
	}

	@Override
	protected Object createDelegate(AnnotationDisplayMode displayMode) {
		switch (displayMode) {
		case SELECTED:
			return selectedAnnotationDelegate;
			
		case FIRST_ONLY:
			return firstAnnotationDelegate;

		case LAST_ONLY:
			return lastAnnotationDelegate;

		case ALL:
			return allAnnotationDelegate;

		default:
			return noneAnnotationDelegate;
		}
	}
	
	protected abstract static class AnnotationDelegate {
		
		protected boolean setId(DependencyAnnotation annotation, int position) {
			// for subclasses
			return true;
		}

		public int getGroupId(DependencyAnnotation annotation, int index, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.getGroupId(index);
			} else {
				return -1;
			}
		}
		
		public int getGroupId(DependencyAnnotation annotation, int index, String token, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.getGroupId(index, token);
			} else {
				return -1;
			}
		}
		
		public boolean isHighlighted(DependencyAnnotation annotation, int index, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.isHighlighted(index);
			} else {
				return false;
			}
		}
		
		public boolean isNodeHighlighted(DependencyAnnotation annotation, int index, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.isNodeHighlighted(index);
			} else {
				return false;
			}
		}
		
		public boolean isEdgeHighlighted(DependencyAnnotation annotation, int index, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.isEdgeHighlighted(index);
			} else {
				return false;
			}
		}
		
		public boolean isTransitiveHighlighted(DependencyAnnotation annotation, int index, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.isTransitiveHighlighted(index);
			} else {
				return false;
			}
		}
		
		public long getHighlight(DependencyAnnotation annotation, int index, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.getHighlight(index);
			} else {
				return 0L;
			}
		}
		
		public boolean isTokenHighlighted(DependencyAnnotation annotation, int index, String token, int position) {
			if(annotation!=null && setId(annotation, position)) {
				return annotation.isTokenHighlighted(index, token);
			} else {
				return false;
			}
		}
	}
	
	protected static final AnnotationDelegate firstAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(DependencyAnnotation annotation, int position) {
			annotation.moveToAnnotation(0);
			return true;
		}
		
	};
	
	protected static final AnnotationDelegate lastAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(DependencyAnnotation annotation, int position) {
			annotation.moveToAnnotation(annotation.getAnnotationCount()-1);
			return true;
		}
		
	};
	
	protected static final AnnotationDelegate selectedAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(DependencyAnnotation annotation, int position) {
			annotation.moveToAnnotation(position);
			return true;
		}
		
	};
	
	protected static final AnnotationDelegate noneAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(DependencyAnnotation annotation, int position) {
			return false;
		}
		
	};
	
	protected static final AnnotationDelegate allAnnotationDelegate = new AnnotationDelegate() {
		
		protected List<Long> buffer;
		
		@Override
		public int getGroupId(DependencyAnnotation annotation, int index, int position) {
			int groupId = -1;
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if((groupId = annotation.getGroupId(index)) != -1) {
					break;
				}
			}
			annotation.moveToAnnotation(position);
			return groupId;
		}

		@Override
		public int getGroupId(DependencyAnnotation annotation, int index, String token, int position) {
			int groupId = -1;
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if((groupId = annotation.getGroupId(index, token)) != -1) {
					break;
				}
			}
			annotation.moveToAnnotation(position);
			return groupId;
		}

		@Override
		public boolean isHighlighted(DependencyAnnotation annotation, int index, int position) {
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(position);
			return false;
		}

		@Override
		public boolean isNodeHighlighted(DependencyAnnotation annotation, int index, int position) {
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isNodeHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(position);
			return false;
		}

		@Override
		public boolean isEdgeHighlighted(DependencyAnnotation annotation, int index, int position) {
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isEdgeHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(position);
			return false;
		}

		@Override
		public boolean isTransitiveHighlighted(DependencyAnnotation annotation, int index, int position) {
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isTransitiveHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(position);
			return false;
		}

		@Override
		public long getHighlight(DependencyAnnotation annotation, int index, int position) {
			if(buffer==null) {
				buffer = new ArrayList<>();
			}
			
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				long highlight = annotation.getHighlight(index);
				if(highlight!=0L) {
					buffer.add(highlight);
				}
			}
			
			if(buffer.isEmpty()) {
				return 0L;
			} else {
				int size = buffer.size();
				long[] highlights = new long[size];
				for(int i=0; i<size; i++) {
					highlights[i] = buffer.get(i);
				}
				buffer.clear();
				
				return DependencyHighlighting.createCompositeHighlight(highlights);
			}
		}

		@Override
		public boolean isTokenHighlighted(DependencyAnnotation annotation,
				int index, String token, int position) {
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isTokenHighlighted(index, token)) {
					return true;
				}
			}
			annotation.moveToAnnotation(position);
			return false;
		}

	};
}
