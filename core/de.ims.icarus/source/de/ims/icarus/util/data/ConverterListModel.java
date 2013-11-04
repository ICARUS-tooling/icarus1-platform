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
public class ConverterListModel extends AbstractListModel<DataConverter> implements
		ComboBoxModel<DataConverter> {

	private static final long serialVersionUID = 1564331993862708895L;
	
	private List<DataConverter> converters = new ArrayList<>();
	
	private DataConverter selectedConverter = null;
	
	public ConverterListModel(Collection<DataConverter> items) {
		if(items==null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$
		
		converters.addAll(items);
	}
	
	public ConverterListModel(DataConverter[] items) {
		if(items==null)
			throw new NullPointerException("Invalid items"); //$NON-NLS-1$
		
		for(DataConverter converter : items) {
			converters.add(converter);
		}
	}

	/**
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return converters.size();
	}

	/**
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public DataConverter getElementAt(int index) {
		return converters.get(index);
	}

	/**
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		if(anItem!=null && !(anItem instanceof DataConverter))
			throw new IllegalArgumentException("Unsupported item: "+anItem); //$NON-NLS-1$
		
		if((selectedConverter!=null && !selectedConverter.equals(anItem))
				|| (selectedConverter==null && anItem!=null)) {
			selectedConverter = (DataConverter) anItem;
			
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return selectedConverter;
	}

}
