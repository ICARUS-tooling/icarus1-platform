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

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.util.annotation.Annotation;
import de.ims.icarus.util.annotation.AnnotationDisplayMode;
import de.ims.icarus.util.annotation.AnnotationManager;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractSearchAnnotationManager extends AnnotationManager {
	
	protected AbstractSearchAnnotationManager() {
		// no-op
	}
	
	protected AnnotationDelegate getDelegate() {
		return (AnnotationDelegate) delegate;
	}

	@Override
	public SearchAnnotation getAnnotation() {
		return (SearchAnnotation) super.getAnnotation();
	}

	public int getGroupId(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? -1 : delegate.getGroupId(
				this, index);
	}
	
	public int getGroupId(int index, String token) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? -1 : delegate.getGroupId(
				this, index);
	}
	
	public boolean isHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isHighlighted(
				this, index);
	}
	
	public boolean isNodeHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isNodeHighlighted(
				this, index);
	}
	
	public boolean isEdgeHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isEdgeHighlighted(
				this, index);
	}
	
	public boolean isTransitiveHighlighted(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isTransitiveHighlighted(
				this, index);
	}
	
	public long getHighlight(int index) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? 0L : delegate.getHighlight(
				this, index);
	}
	
	public boolean isTokenHighlighted(int index, String token) {
		AnnotationDelegate delegate = getDelegate();
		return delegate==null ? false : delegate.isTokenHighlighted(
				this, index, token);
	}
	
	protected abstract long createCompositeHighlight(long[] highlights);

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
		
		protected boolean setId(AbstractSearchAnnotationManager manager) {
			// for subclasses
			return true;
		}

		public int getGroupId(AbstractSearchAnnotationManager manager, int index) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().getGroupId(index);
			} else {
				return -1;
			}
		}
		
		public int getGroupId(AbstractSearchAnnotationManager manager, int index, String token) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().getGroupId(index, token);
			} else {
				return -1;
			}
		}
		
		public boolean isHighlighted(AbstractSearchAnnotationManager manager, int index) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().isHighlighted(index);
			} else {
				return false;
			}
		}
		
		public boolean isNodeHighlighted(AbstractSearchAnnotationManager manager, int index) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().isNodeHighlighted(index);
			} else {
				return false;
			}
		}
		
		public boolean isEdgeHighlighted(AbstractSearchAnnotationManager manager, int index) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().isEdgeHighlighted(index);
			} else {
				return false;
			}
		}
		
		public boolean isTransitiveHighlighted(AbstractSearchAnnotationManager manager, int index) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().isTransitiveHighlighted(index);
			} else {
				return false;
			}
		}
		
		public long getHighlight(AbstractSearchAnnotationManager manager, int index) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().getHighlight(index);
			} else {
				return 0L;
			}
		}
		
		public boolean isTokenHighlighted(AbstractSearchAnnotationManager manager, int index, String token) {
			if(manager.hasAnnotation() && setId(manager)) {
				return manager.getAnnotation().isTokenHighlighted(index, token);
			} else {
				return false;
			}
		}
	}
	
	protected static final AnnotationDelegate firstAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(AbstractSearchAnnotationManager manager) {
			manager.getAnnotation().moveToAnnotation(0);
			return true;
		}
		
	};
	
	protected static final AnnotationDelegate lastAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(AbstractSearchAnnotationManager manager) {
			Annotation annotation = manager.getAnnotation();
			annotation.moveToAnnotation(annotation.getAnnotationCount()-1);
			return true;
		}
		
	};
	
	protected static final AnnotationDelegate selectedAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(AbstractSearchAnnotationManager manager) {
			manager.getAnnotation().moveToAnnotation(manager.getPosition());
			return true;
		}
		
	};
	
	protected static final AnnotationDelegate noneAnnotationDelegate = new AnnotationDelegate() {

		@Override
		protected boolean setId(AbstractSearchAnnotationManager manager) {
			return false;
		}
		
	};
	
	protected static final AnnotationDelegate allAnnotationDelegate = new AnnotationDelegate() {
		
		protected List<Long> buffer;
		
		@Override
		public int getGroupId(AbstractSearchAnnotationManager manager, int index) {
			SearchAnnotation annotation = manager.getAnnotation();
			int groupId = -1;
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if((groupId = annotation.getGroupId(index)) != -1) {
					break;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return groupId;
		}

		@Override
		public int getGroupId(AbstractSearchAnnotationManager manager, int index, String token) {
			SearchAnnotation annotation = manager.getAnnotation();
			int groupId = -1;
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if((groupId = annotation.getGroupId(index, token)) != -1) {
					break;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return groupId;
		}

		@Override
		public boolean isHighlighted(AbstractSearchAnnotationManager manager, int index) {
			SearchAnnotation annotation = manager.getAnnotation();
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return false;
		}

		@Override
		public boolean isNodeHighlighted(AbstractSearchAnnotationManager manager, int index) {
			SearchAnnotation annotation = manager.getAnnotation();
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isNodeHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return false;
		}

		@Override
		public boolean isEdgeHighlighted(AbstractSearchAnnotationManager manager, int index) {
			SearchAnnotation annotation = manager.getAnnotation();
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isEdgeHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return false;
		}

		@Override
		public boolean isTransitiveHighlighted(AbstractSearchAnnotationManager manager, int index) {
			SearchAnnotation annotation = manager.getAnnotation();
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isTransitiveHighlighted(index)) {
					return true;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return false;
		}

		@Override
		public long getHighlight(AbstractSearchAnnotationManager manager, int index) {
			if(buffer==null) {
				buffer = new ArrayList<>();
			}

			SearchAnnotation annotation = manager.getAnnotation();
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				long highlight = annotation.getHighlight(index);
				if(highlight!=0L) {
					buffer.add(highlight);
				}
			}
			
			annotation.moveToAnnotation(manager.getPosition());
			
			if(buffer.isEmpty()) {
				return 0L;
			} else {
				int size = buffer.size();
				long[] highlights = new long[size];
				for(int i=0; i<size; i++) {
					highlights[i] = buffer.get(i);
				}
				buffer.clear();
				
				return manager.createCompositeHighlight(highlights);
			}
		}

		@Override
		public boolean isTokenHighlighted(AbstractSearchAnnotationManager manager, int index, String token) {
			SearchAnnotation annotation = manager.getAnnotation();
			for(int i=0; i<annotation.getAnnotationCount(); i++) {
				annotation.moveToAnnotation(i);
				if(annotation.isTokenHighlighted(index, token)) {
					return true;
				}
			}
			annotation.moveToAnnotation(manager.getPosition());
			return false;
		}

	};

}
