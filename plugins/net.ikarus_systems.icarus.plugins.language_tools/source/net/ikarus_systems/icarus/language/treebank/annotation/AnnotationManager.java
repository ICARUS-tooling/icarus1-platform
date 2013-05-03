/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.treebank.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import net.ikarus_systems.icarus.util.Exceptions;
import net.ikarus_systems.icarus.util.PropertyChangeSource;

/**
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 * @param <A>
 */
public abstract class AnnotationManager<A extends Object> 
		extends PropertyChangeSource {

	protected A annotation;
	
	protected AnnotationDisplayMode displayMode;
	
	protected int traversalIndex = ResultAnnotation.AFTER_LAST;
	
	protected AnnotationManager() {
	}
	
	protected AnnotationManager(AnnotationManager<A> parent) {
		Exceptions.testNullArgument(parent, "parent"); //$NON-NLS-1$
		
		parent.addPropertyChangeListener(new ParentPropertyListener(this));
		
		copyState(parent);
	}
	
	@SuppressWarnings("unchecked")
	protected void copyState(AnnotationManager<?> source) {
		Object oldAnnotation = annotation;
		AnnotationDisplayMode oldDisplayMode = displayMode;
		int oldTraversalIndex = traversalIndex;
		
		annotation = (A) source.annotation;
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
	public A getAnnotation() {
		return annotation;
	}

	/**
	 * @param annotation the annotation to set
	 */
	@SuppressWarnings("unchecked")
	public void setAnnotation(Object annotation) {
		Exceptions.testNullArgument(annotation, "annotation"); //$NON-NLS-1$
		
		if(!annotation.equals(this.annotation)) {
			
			Object oldValue = this.annotation;
			this.annotation = (A) annotation;
			
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
		case FIRST_ANNOTATION_ONLY:
			return 0;
			
		case LAST_ANNOTATION_ONLY:
			return getMaxTraversalIndex();
			
		case ALL_ANNOTATIONS:
			return ResultAnnotation.AFTER_LAST;
			
		case SELECTED_ANNOTATION:
			return traversalIndex;
			
		case NO_ANNOTATIONS:
			return ResultAnnotation.BEFORE_FIRST;
			
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
	
	public abstract AnnotationManager<A> linkedCopy();
	
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
		
		private final WeakReference<AnnotationManager<?>> ref;
		
		ParentPropertyListener(AnnotationManager<?> manager) {
			Exceptions.testNullArgument(manager, "manager"); //$NON-NLS-1$
			
			this.ref = new WeakReference<AnnotationManager<?>>(manager);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			
			AnnotationManager<?> parent = (AnnotationManager<?>)evt.getSource();
			
			AnnotationManager<?> target = ref.get();
			if(target==null)
				parent.removePropertyChangeListener(this);
			else 
				target.copyState(parent);
		}
		
	}
}
