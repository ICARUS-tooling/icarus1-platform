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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConverterListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 5703569418108633970L;

	public ConverterListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		DataConverter converter = null;
		
		if(value instanceof DataConverter) {
			converter = (DataConverter)value;
		}
		
		if(converter!=null) {
			value = converter.getInputType().getName()
					+" >> "+converter.getResultType().getName() //$NON-NLS-1$
					+" ("+converter.getAccuracy()+")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		return this;
	}

}
