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
package de.ims.icarus.util.mpi;

/**
 * A collection of commonly used commands.
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Commands {

	/**
	 * Perform a selective action on the current data according to
	 * the transmitted hint. Typical parameters are an integer
	 * index pointing to the data item that has to be selected
	 * or the item itself so that the target handler has to find
	 * the matching index itself.
	 */
	public static final String SELECT = "select"; //$NON-NLS-1$
	
	/**
	 * Makes an arbitrary kind of visualization based on the
	 * data parameter. Unlike the {@link #PRESENT} command this
	 * does not imply the option of interaction from the user side.
	 * It is up to the target implementation how much the user will
	 * be allowed to interact with the data.
	 */
	public static final String DISPLAY = "display"; //$NON-NLS-1$
	
	/**
	 * Makes an arbitrary kind of visualization based on the
	 * data parameter. If the data being passed as parameter is mutable
	 * then the user should be presented with tools and options that allow
	 * him to modify the data.
	 */
	public static final String PRESENT = "present"; //$NON-NLS-1$
	
	/**
	 * Starts an 'edit' operation on the data parameter. Unlike the
	 * {@link #PRESENT} command that also allows user-side modifications
	 * of data the data is not necessarily visualized to the user, he only
	 * has to be presented a collection of low-level tools to access its state. 
	 */
	public static final String EDIT = "edit"; //$NON-NLS-1$

	/**
	 * All internal data should be reverted to some known 'default'
	 * state. This includes all performed visualizations.
	 */
	public static final String CLEAR = "clear"; //$NON-NLS-1$

	public static final String GET = "get"; //$NON-NLS-1$
	public static final String SET = "set"; //$NON-NLS-1$

	/**
	 * 
	 */
	public static final String GET_TEXT = "get-text"; //$NON-NLS-1$
	public static final String SET_TEXT = "set-text"; //$NON-NLS-1$
	public static final String APPEND = "append"; //$NON-NLS-1$
	
	/**
	 * Tells a target object that is assigned the task of modifying
	 * some data to instantly commit any pending changes to that data structure.
	 * This command is typically used to synchronize independent editor
	 * implementations that operate on a common data structure.
	 */
	public static final String COMMIT = "commit"; //$NON-NLS-1$
}
