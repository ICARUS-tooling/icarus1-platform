/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.util.data;

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
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
		converters.addAll(items);
	}
	
	public ConverterListModel(DataConverter[] items) {
		if(items==null)
			throw new IllegalArgumentException("Invalid items"); //$NON-NLS-1$
		
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
