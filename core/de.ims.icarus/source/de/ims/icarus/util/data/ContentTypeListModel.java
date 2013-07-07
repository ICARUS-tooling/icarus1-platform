/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.util.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ContentTypeListModel extends AbstractListModel<ContentType> 
		implements ComboBoxModel<ContentType> {
	
	private static final long serialVersionUID = 9140005015379485387L;
	
	private List<ContentType> types = new ArrayList<>();
	
	private ContentType selectedType = null;
	
	public ContentTypeListModel(Collection<ContentType> items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		types.addAll(items);
	}
	
	public ContentTypeListModel(ContentType[] items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		for(ContentType type : items) {
			types.add(type);
		}
	}
	
	public ContentTypeListModel(String[] ids) {
		if(ids==null)
			throw new IllegalArgumentException("Invalid ids"); //$NON-NLS-1$
		
		for(String id : ids) {
			types.add(ContentTypeRegistry.getInstance().getType(id));
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return types.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public ContentType getElementAt(int index) {
		return types.get(index);
	}

	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		if(anItem!=null && !(anItem instanceof ContentType))
			throw new IllegalArgumentException("Unsupported item: "+anItem); //$NON-NLS-1$
		
		if((selectedType!=null && !selectedType.equals(anItem))
				|| (selectedType==null && anItem!=null)) {
			selectedType = (ContentType) anItem;
			
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return selectedType;
	}
}