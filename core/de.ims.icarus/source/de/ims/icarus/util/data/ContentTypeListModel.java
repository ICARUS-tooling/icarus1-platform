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
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$
		
		types.addAll(items);
	}
	
	public ContentTypeListModel(ContentType[] items) {
		if(items==null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$
		
		for(ContentType type : items) {
			types.add(type);
		}
	}
	
	public ContentTypeListModel(String[] ids) {
		if(ids==null)
			throw new NullPointerException("Invalid ids"); //$NON-NLS-1$
		
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