/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.annotation;

import de.ims.icarus.util.Exceptions;
import de.ims.icarus.util.PropertyChangeSource;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 * @param <A>
 */
public abstract class AnnotationManager 
		extends PropertyChangeSource {

	protected Annotation annotation;
	
	protected AnnotationDisplayMode displayMode;
	
	protected int position = Annotation.AFTER_LAST;
	
	protected Object delegate;
	
	protected AnnotationManager() {
		setDisplayMode(AnnotationDisplayMode.FIRST_ONLY);
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		if(annotation!=null) {
			ContentType contentType = ContentTypeRegistry.getInstance().getEnclosingType(annotation);
			if(contentType==null || !supportsAnnotation(contentType))
				throw new IllegalArgumentException("Unsupported annotation type: "+annotation.getClass()); //$NON-NLS-1$
		}
		
		if(annotation!=this.annotation) {
			Object oldValue = this.annotation;
			this.annotation = annotation;
			
			position = annotation==null ? Annotation.AFTER_LAST : 0;
			
			firePropertyChange("annotation", oldValue, annotation); //$NON-NLS-1$
		}
	}

	public AnnotationDisplayMode getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(AnnotationDisplayMode displayMode) {
		Exceptions.testNullArgument(displayMode, "displayMode"); //$NON-NLS-1$
		
		if(displayMode!=this.displayMode) {
			
			Object oldValue = this.displayMode;
			this.displayMode = displayMode;
			
			delegate = createDelegate(displayMode);
			
			firePropertyChange("displayMode", oldValue, displayMode); //$NON-NLS-1$
		}
	}
	
	protected Object createDelegate(AnnotationDisplayMode displayMode) {
		return null;
	}
	
	public boolean hasAnnotation() {
		return annotation!=null && annotation.getAnnotationCount()>0;
	}

	public int getPosition() {
		switch (getDisplayMode()) {
		case FIRST_ONLY:
			return 0;
			
		case LAST_ONLY:
			return getMaxPosition();
			
		case ALL:
			return Annotation.AFTER_LAST;
			
		case SELECTED:
			return position;
			
		case NONE:
			return Annotation.BEFORE_FIRST;
			
		default:
			throw new IllegalStateException();
		}
	}
	
	private void checkPosition(int position) {
		if(position<0 || position>getMaxPosition())
			throw new IndexOutOfBoundsException();
	}

	public void setPosition(int position) {
		if(position != this.position) {
			checkPosition(position);
			
			int oldValue = this.position;
			this.position = position;
			
			firePropertyChange("position", oldValue, position); //$NON-NLS-1$
		}
	}
	
	public int getMaxPosition() {
		return annotation==null ? -1 : annotation.getAnnotationCount()-1;
	}
	
	public void previous() {
		if(!isFirst()) {
			int oldValue = position;
			position--;

			firePropertyChange("position", oldValue, position); //$NON-NLS-1$
		}
	}
	
	public void next() {
		if(!isLast()) {
			int oldValue = position;
			position++;

			firePropertyChange("position", oldValue, position); //$NON-NLS-1$
		}
	}
	
	public void first() {
		if(!isFirst()) {
			int oldValue = position;
			position = 0;

			firePropertyChange("position", oldValue, position); //$NON-NLS-1$
		}
	}
	
	public void last() {
		if(!isLast()) {
			int oldValue = position;
			position = getMaxPosition();

			firePropertyChange("position", oldValue, position); //$NON-NLS-1$
		}
	}
	
	public boolean isLast() {
		return position == getMaxPosition(); 
	}
	
	public boolean isFirst() {
		return position == 0; 
	}
	
	public abstract ContentType getAnnotationType();
	
	public boolean supportsAnnotation(ContentType contentType) {
		if(contentType==null) {
			return false;
		}
		return ContentTypeRegistry.isCompatible(getAnnotationType(), contentType);
	}
	
	/**
	 * Returns the total number of 'hits' the underlying
	 * annotation currently represents. Depending on the 
	 * current {@code displayMode} this number may vary.
	 * Note:<p> 
	 * Since it is possible that multiple 'hits' point
	 * to the same object (form) in an annotated data
	 * object the returned number should be used as upper
	 * bound (the actual number of items to be visually
	 * annotated can be smaller).
	 * @return
	 */
	public int getDisplayedAnnotationCount() {
		if(annotation==null || annotation.getAnnotationCount()==0) {
			return 0;
		}
		
		switch (displayMode) {
		case ALL:
			return annotation.getAnnotationCount();
		case NONE:
			return 0;
		case FIRST_ONLY:
			return 1;
		case LAST_ONLY:
			return 1;
		case SELECTED:
			return 1;
		}
		
		return 0;
	}
}
