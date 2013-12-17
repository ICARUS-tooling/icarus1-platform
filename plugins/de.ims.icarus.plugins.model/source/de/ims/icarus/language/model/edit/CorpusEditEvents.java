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
package de.ims.icarus.language.model.edit;

import javax.swing.event.UndoableEditListener;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface CorpusEditEvents {

	/**
	 * Fired when the update level is increased
	 */
	public static final String BEGIN_UPDATE = "beginUpdate"; //$NON-NLS-1$

	/**
	 * Fired when the update level is decreased.
	 * <p>
	 * The "edit" property contains the edit in progress.
	 */
	public static final String END_UPDATE = "endUpdate"; //$NON-NLS-1$

	/**
	 * Fired when an edit is executed.
	 */
	public static final String EXECUTE = "execute"; //$NON-NLS-1$

	/**
	 * Fired after an edit has been executed but before it is
	 * dispatched to the {@link UndoableEditListener}s.
	 */
	public static final String BEFORE_UNDO = "beforeUndo"; //$NON-NLS-1$

	/**
	 * Fired when the
	 */
	public static final String UNDO = "undo"; //$NON-NLS-1$
	public static final String CHANGE = "change"; //$NON-NLS-1$
}
