/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.ui.view;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import net.ikarus_systems.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ListPresenter extends AWTPresenter {
	
	ListModel<?> getListModel();
	
	ListSelectionModel getSelectionModel();
	
	/**
	 * Returns the content type encapsulating the elements
	 * in the internal list or {@code null} if this presenter
	 * is unaware of that content type. 
	 */
	ContentType getContentType();
}