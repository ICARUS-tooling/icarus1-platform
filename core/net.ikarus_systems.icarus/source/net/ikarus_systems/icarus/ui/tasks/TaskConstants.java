/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.tasks;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface TaskConstants {
	
	// Properties

	/**
	 * Used to indicate a change in the task's info 
	 * (description).
	 */
	public static final String INFO_PROPERTY = "info"; //$NON-NLS-1$

	/**
	 * Used to indicate a change in the task's title.
	 */
	public static final String TITLE_PROPERTY = "title"; //$NON-NLS-1$

	/**
	 * Used to indicate a change in the task's icon.
	 */
	public static final String ICON_PROPERTY = "icon"; //$NON-NLS-1$

	/**
	 * Used to indicate a change in the task's indeterminate state.
	 */
	public static final String INDETERMINATE_PROPERTY = "indeterminate"; //$NON-NLS-1$

	/**
	 * Used to indicate that more than one property of
	 * a given task has changed.
	 */
	public static final String TASK_PROPERTY = "task"; //$NON-NLS-1$
	

	// SwingWorker properties
	
	public static final String STATE_PROPERTY = "state"; //$NON-NLS-1$

	public static final String PROGRESS_PROPERTY = "progress"; //$NON-NLS-1$
	
	// Events
	
	/**
	 * Indicates a change in the active task
	 */
	public static final String ACTIVE_TASK_CHANGED = "activeTask"; //$NON-NLS-1$
}
