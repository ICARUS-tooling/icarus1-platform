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
