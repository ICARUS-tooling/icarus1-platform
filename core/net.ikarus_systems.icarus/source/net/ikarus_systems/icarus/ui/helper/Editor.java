/*
 * $Revision: 23 $
 * $Date: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/helper/Editor.java $
 *
 * $LastChangedDate: 2013-04-17 14:39:04 +0200 (Mi, 17 Apr 2013) $ 
 * $LastChangedRevision: 23 $ 
 * $LastChangedBy: mcgaerty $
 */
package net.ikarus_systems.icarus.ui.helper;

import java.awt.Component;

/**
 * @author Markus Gärtner
 * @version $Id: Editor.java 23 2013-04-17 12:39:04Z mcgaerty $
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
	 * @version $Id: Editor.java 23 2013-04-17 12:39:04Z mcgaerty $
	 *
	 * @param <T>
	 */
	public interface TableEditor<T extends Object> extends Editor<T> {
		
	}
	
	/**
	 * 
	 * @author Markus Gärtner
	 * @version $Id: Editor.java 23 2013-04-17 12:39:04Z mcgaerty $
	 *
	 * @param <T>
	 */
	public interface GraphEditor<T extends Object> extends Editor<T> {
		
	}
}
