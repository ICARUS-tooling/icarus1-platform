/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.ui.tasks;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public enum TaskPriority {

	/**
	 * Basically a task scheduled with priority {@code NONE}
	 * is considered to be optional. It can be cancelled by the
	 * executing {@code TaskManager} at any time and is not guaranteed
	 * to get executed at all.
	 */
	NONE(Integer.MIN_VALUE),
	
	LOW(-100),
	
	DEFAULT(0),
	
	HIGH(100),
	
	/**
	 * Great care should be taken when scheduling tasks with
	 * priority {@code INSTANT}! When a {@code TaskManager} 
	 * receives a task with this priority it will cancel the
	 * currently executed task if it is not of the same
	 * priority and then immediately run the new task, totally
	 * ignoring any other pending tasks. Note that a task which
	 * gets cancelled and is not enabled to be re-scheduled might
	 * loose all its progress and its work will not be completed!
	 * <p>
	 * This priority value is not used by the {@code TaskManager}
	 * as of now!
	 */
	INSTANT(Integer.MAX_VALUE);
	
	private final int value;
	
	private TaskPriority(int value) {
		this.value = value;
	}
	
	public int intValue() {
		return value;
	}
}
