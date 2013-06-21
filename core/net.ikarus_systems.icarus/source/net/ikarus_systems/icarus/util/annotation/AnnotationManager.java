/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.PropertyChangeSource;
import net.ikarus_systems.icarus.util.data.ContentType;

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
	
	protected int traversalIndex = Annotation.AFTER_LAST;
	
	protected AnnotationManager() {
	}
	
	protected AnnotationManager(AnnotationManager parent) {
		Exceptions.testNullArgument(parent, "parent"); //$NON-NLS-1$
		
		parent.addPropertyChangeListener(new ParentPropertyListener(this));
		
		copyState(parent);
	}
	
	protected void copyState(AnnotationManager source) {
		Object oldAnnotation = annotation;
		AnnotationDisplayMode oldDisplayMode = displayMode;
		int oldTraversalIndex = traversalIndex;
		
		annotation = source.annotation;
		displayMode = source.displayMode;
		traversalIndex = source.traversalIndex;
		
		annotationChanged();
		displayModeChanged();
		traversalIndexChanged();
		
		if(oldAnnotation != annotation)
			firePropertyChange("annotation", oldAnnotation, annotation); //$NON-NLS-1$
		if(oldDisplayMode != displayMode)
			firePropertyChange("displayMode", oldDisplayMode, displayMode); //$NON-NLS-1$
		if(oldTraversalIndex != traversalIndex)
			firePropertyChange("traversalIndex", oldTraversalIndex, traversalIndex); //$NON-NLS-1$
	}

	/**
	 * @return the annotation
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/**
	 * @param annotation the annotation to set
	 */
	public void setAnnotation(Annotation annotation) {
		Exceptions.testNullArgument(annotation, "annotation"); //$NON-NLS-1$
		
		if(!annotation.equals(this.annotation)) {
			
			Object oldValue = this.annotation;
			this.annotation = annotation;
			
			annotationChanged();
			
			firePropertyChange("annotation", oldValue, annotation); //$NON-NLS-1$
		}
	}
	
	protected abstract void annotationChanged();

	/**
	 * @return the displayMode
	 */
	public AnnotationDisplayMode getDisplayMode() {
		return displayMode;
	}

	/**
	 * @param displayMode the displayMode to set
	 */
	public void setDisplayMode(AnnotationDisplayMode displayMode) {
		Exceptions.testNullArgument(displayMode, "displayMode"); //$NON-NLS-1$
		
		if(displayMode!=this.displayMode) {
			
			Object oldValue = this.displayMode;
			this.displayMode = displayMode;
			
			displayModeChanged();
			
			firePropertyChange("displayMode", oldValue, displayMode); //$NON-NLS-1$
		}
	}
	
	protected abstract void displayModeChanged();

	/**
	 * @return the traversalIndex
	 */
	public int getTraversalIndex() {
		switch (getDisplayMode()) {
		case FIRST_ONLY:
			return 0;
			
		case LAST_ONLY:
			return getMaxTraversalIndex();
			
		case ALL:
			return Annotation.AFTER_LAST;
			
		case SELECTED:
			return traversalIndex;
			
		case NONE:
			return Annotation.BEFORE_FIRST;
			
		default:
			throw new IllegalStateException();
		}
	}
	
	private void checkIndex(int index) {
		if(index<0 || index>=getMaxTraversalIndex())
			throw new IndexOutOfBoundsException();
	}

	/**
	 * @param traversalIndex the traversalIndex to set
	 */
	public void setTraversalIndex(int traversalIndex) {
		if(traversalIndex != this.traversalIndex) {
			checkIndex(traversalIndex);
			
			int oldValue = this.traversalIndex;
			this.traversalIndex = traversalIndex;
			
			traversalIndexChanged();
			
			firePropertyChange("traversalIndex", oldValue, traversalIndex); //$NON-NLS-1$
		}
	}
	
	protected abstract void traversalIndexChanged();
	
	public abstract int getMaxTraversalIndex();
	
	public void previous() {
		if(!isFirst()) {
			int oldValue = traversalIndex;
			traversalIndex--;
			
			traversalIndexChanged();

			firePropertyChange("traversalIndex", oldValue, traversalIndex); //$NON-NLS-1$
		}
	}
	
	public void next() {
		if(!isLast()) {
			int oldValue = traversalIndex;
			traversalIndex++;
			
			traversalIndexChanged();

			firePropertyChange("traversalIndex", oldValue, traversalIndex); //$NON-NLS-1$
		}
	}
	
	public void first() {
		if(!isFirst()) {
			int oldValue = traversalIndex;
			traversalIndex = 0;
			
			traversalIndexChanged();

			firePropertyChange("traversalIndex", oldValue, traversalIndex); //$NON-NLS-1$
		}
	}
	
	public void last() {
		if(!isLast()) {
			int oldValue = traversalIndex;
			traversalIndex = getMaxTraversalIndex();
			
			traversalIndexChanged();

			firePropertyChange("traversalIndex", oldValue, traversalIndex); //$NON-NLS-1$
		}
	}
	
	public boolean isLast() {
		return traversalIndex == getMaxTraversalIndex(); 
	}
	
	public boolean isFirst() {
		return traversalIndex == 0; 
	}
	
	public abstract AnnotationManager linkedCopy();
	
	public abstract boolean supportsAnnotation(ContentType contentType);
	
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
	public abstract int getDisplayedAnnotationCount();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	protected static class ParentPropertyListener implements PropertyChangeListener {
		
		private final WeakReference<AnnotationManager> ref;
		
		ParentPropertyListener(AnnotationManager manager) {
			Exceptions.testNullArgument(manager, "manager"); //$NON-NLS-1$
			
			this.ref = new WeakReference<AnnotationManager>(manager);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			
			AnnotationManager parent = (AnnotationManager)evt.getSource();
			
			if(ref.get()==null)
				parent.removePropertyChangeListener(this);
			else 
				ref.get().copyState(parent);
		}
		
	}
}
