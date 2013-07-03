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

import java.awt.Component;

import javax.swing.JList;

import net.ikarus_systems.icarus.ui.helper.TooltipListCellRenderer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DataListCellRenderer extends TooltipListCellRenderer {

	private static final long serialVersionUID = -3488624857141838661L;
	
	private static DataListCellRenderer sharedInstance;
	
	public static DataListCellRenderer getSharedIntance() {
		if(sharedInstance==null) {
			sharedInstance = new DataListCellRenderer();
		}
		return sharedInstance;
	}

	public DataListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		value = (index+1)+": "+value; //$NON-NLS-1$
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		return this;
	}

}
