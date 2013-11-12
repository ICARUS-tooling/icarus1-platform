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
package de.ims.icarus.language.coref.registry;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import de.ims.icarus.ui.Updatable;

public class AllocationListWrapper extends AbstractListModel<Object> 
		implements ComboBoxModel<Object>, Updatable {

	private static final long serialVersionUID = 5697820922840356053L;
	
	private final boolean gold;
	
	private Object selectedItem;

	private DocumentSetDescriptor descriptor;
	
	public AllocationListWrapper(boolean gold) {
		this.gold = gold;
	}
	
	private boolean isEmpty() {
		return descriptor==null || descriptor.size()==0;
	}
	
	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		int offset = gold ? 2 : 1;
		return isEmpty() ? 0 : descriptor.size() + offset;
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		if(isEmpty()) {
			return null;
		}
		if(index==0) {
			return gold ? CoreferenceRegistry.dummyEntry : 
				getDefaultAllocationDescriptor();
		} else if(gold && index==1) {
			return getDefaultAllocationDescriptor();
		}
		int offset = gold ? 2 : 1;
		return descriptor.get(index-offset);
	}

	/**
	 * @return
	 */
	private Object getDefaultAllocationDescriptor() {
		return CoreferenceRegistry.getInstance()
				.getDefaultAllocationDescriptor(descriptor);
	}

	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		
		if(anItem==CoreferenceRegistry.dummyEntry) {
			anItem = null;
		}
		
		if((selectedItem!=null && !selectedItem.equals(anItem))
				|| (selectedItem==null && anItem!=null)) {
			
			selectedItem = anItem;
			
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		if(selectedItem==null) {
			return gold ? CoreferenceRegistry.dummyEntry
					: getDefaultAllocationDescriptor();
		}
		
		return selectedItem;
	}
	
	/**
	 * @see de.ims.icarus.ui.Updatable#update()
	 */
	@Override
	public boolean update() {
		fireContentsChanged(this, 0, Math.max(0, getSize()-1));
		return true;
	}

	/**
	 * @return the descriptor
	 */
	public DocumentSetDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @param descriptor the descriptor to set
	 */
	public void setDescriptor(DocumentSetDescriptor descriptor) {
		
		this.descriptor = descriptor;
		
		if(selectedItem instanceof DefaultAllocationDescriptor) {
			selectedItem = getDefaultAllocationDescriptor();
		}
	}
}