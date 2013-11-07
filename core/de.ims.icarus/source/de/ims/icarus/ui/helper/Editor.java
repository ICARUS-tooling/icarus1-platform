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
package de.ims.icarus.ui.helper;

import java.awt.Component;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Editor<T extends Object> {
	
	/**
	 * Returns the {@code Component} this editor uses to
	 * present its user interface. This method must not return
	 * {@code null} values.
	 * <p>
	 * Note that it is not required for an editor to always return
	 * the same {@code Component}! Implementations using an editor are
	 * advised to retrieve the latest component used by an editor whenever
	 * they intend to display it. 
	 */
	Component getEditorComponent();
	
	/**
	 * Resets the editor to use the supplied {@code item}. It is legal to
	 * provide {@code null} values in which case the editor should simply
	 * clear its interface. If the supplied {@code item} is not of a supported
	 * type then the editor should throw an {@link IllegalArgumentException}.
	 */
	void setEditingItem(T item);
	
	/**
	 * Returns the object last set by {@link #setEditingItem(Object)} or
	 * {@code null} if this editor has not been assigned any items yet.
	 */
	T getEditingItem();
	
	/**
	 * Discards all user input and reloads the appearance based on the
	 * data last set via {@link #setEditingItem(Object)}. If no data is
	 * set to be edited then the editor should present a "blank" interface.
	 */
	void resetEdit();
	
	/**
	 * Applies the changes made by the user to the underlying object to
	 * be edited.
	 */
	void applyEdit();
	
	/**
	 * Compares the current <i>presented state</i> (including potential
	 * user input) with the object last set via {@link #setEditingItem(Object)}
	 * and returns {@code true} if and only if there is a difference between 
	 * those two. If no object has been set for being edited then this method
	 * should return {@code false}.
	 */
	boolean hasChanges();
	
	/**
	 * Tells the editor to release all resources held by it and to
	 * unregister all listeners. After an editor has been closed it is
	 * no longer considered to be usable.
	 */
	void close();
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 * @param <T>
	 */
	public interface TableEditor<T extends Object> extends Editor<T> {
		// no-op
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 * @param <T>
	 */
	public interface GraphEditor<T extends Object> extends Editor<T> {
		// no-op
	}
}
