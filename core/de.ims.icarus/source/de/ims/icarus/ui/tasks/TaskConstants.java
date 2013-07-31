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
package de.ims.icarus.ui.tasks;

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
