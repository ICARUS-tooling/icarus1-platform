/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class SearchUtilityListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -2260987950773277993L;

	public SearchUtilityListCellRenderer() {
		// no-op
	}

	/**
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list,
			Object value, int index, boolean isSelected, boolean cellHasFocus) {
		
		String tooltip = null;
		
		if(value instanceof SearchOperator) {
			SearchOperator operator = (SearchOperator)value;
			value = operator.getSymbol();
			tooltip = operator.getDescription();
		} else if(value instanceof Identity) {
			Identity identity = (Identity) value;
			value = identity.getName();
			tooltip = identity.getDescription();
		}
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
		setToolTipText(UIUtil.toSwingTooltip(tooltip));
		
		return this;
	}

}
